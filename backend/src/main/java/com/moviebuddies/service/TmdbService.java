package com.moviebuddies.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.moviebuddies.entity.Actor;
import com.moviebuddies.entity.Genre;
import com.moviebuddies.entity.Movie;
import com.moviebuddies.repository.ActorRepository;
import com.moviebuddies.repository.GenreRepository;
import com.moviebuddies.repository.MovieRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * TMDB API 연동 서비스
 * 영화, 장르, 배우 데이터를 TMDB에서 가져와 로컬 데이터베이스에 동기화
 *
 * 데이터베이스 동기화 과정:
 * 1. 장르 정보 동기화 (TMDB 장르 ID와 매핑)
 * 2. 영화 기본 정보 동기화 (인기 영화, 현재 상영작)
 * 3. 영화 상세 정보 동기화
 * 4. 출연진 정보 동기화 (주요 배우 10명)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TmdbService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final MovieRepository movieRepository;
    private final GenreRepository genreRepository;
    private final ActorRepository actorRepository;

    @Value("${tmdb.api-key}")
    private String apiKey;

    @Value("${tmdb.base-url}")
    private String baseUrl;

    @Value("${tmdb.image-url}")
    private String imageBaseUrl;

    /**
     * 1단계: 장르 데이터 동기화
     * TMDB의 모든 영화 장르를 가져와 로컬 DB에 저장
     *
     * 동기화 과정:
     * 1. TMDB /genre/movie/list API 호출
     * 2. 응답에서 장르 배열 추출
     * 3. 각 장르의 TMDB ID와 이름을 파싱
     * 4. 기존 장르가 있으면 업데이트, 없으면 새로 생성
     * 5. 데이터베이스에 저장
     */
    @Transactional
    public void syncGenres() {
        log.info("TMDB 장르 데이터 동기화 시작");

        try {
            // TMDB 장르 목록 API URL 구성
            String url = UriComponentsBuilder.fromUriString(baseUrl + "/genre/movie/list")
                    .queryParam("api_key", apiKey)
                    .queryParam("language", "ko-KR")    // 한국어 장르명
                    .toUriString();

            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            JsonNode rootNode = objectMapper.readTree(response.getBody());
            JsonNode genresNode = rootNode.get("genres");

            if (genresNode != null && genresNode.isArray()) {
                for (JsonNode genreNode : genresNode) {
                    Long tmdbId = genreNode.get("id").asLong();
                    String name = genreNode.get("name").asText();

                    // 기존 장르 찾기 또는 새 장르 생성
                    Genre genre = genreRepository.findByTmdbId(tmdbId)
                            .orElse(new Genre());

                    genre.setTmdbId(tmdbId);
                    genre.setName(name);

                    genreRepository.save(genre);
                }

                log.info("TMDB 장르 데이터 동기화 완료");
            }

        } catch (Exception e) {
            log.info("TMDB 장르 데이터 동기화 실패", e);
        }
    }

    /**
     * 2단계: 인기 영화 데이터 동기화
     * TMDB의 인기 영화 목록을 페이지별로 가져와 저장
     *
     * 동기화 과정:
     * 1. 지정된 페이지 수만큼 반복 호출
     * 2. 각 페이지의 영화 목록 파싱
     * 3. processMovieData()로 개별 영화 처리
     * 4. API 호출 제한을 위해 0.04초 대기 (초당 25회 제한)
     *
     * @param totalPages 동기화할 총 페이지 수 (페이지당 20개 영화)
     */
    @Transactional
    public void syncPopularMovies(int totalPages) {
        log.info("TMDB 인기 영화 데이터 동기화 시작 - 페이지 수: {}", totalPages);

        for (int page = 1; page <= totalPages; page++) {
            try {
                String url = UriComponentsBuilder.fromUriString(baseUrl + "/movie/popular")
                        .queryParam("api_key", apiKey)
                        .queryParam("language", "ko-KR")
                        .queryParam("region", "KR")    // 한국 지역 설정
                        .queryParam("page", page)
                        .toUriString();

                ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
                JsonNode rootNode = objectMapper.readTree(response.getBody());
                JsonNode moviesNode = rootNode.get("results");

                if (moviesNode != null && moviesNode.isArray()) {
                    for (JsonNode movieNode : moviesNode) {
                        processMovieData(movieNode, false);   // 일반 인기 영화로 처리
                    }
                }

                // API 호출 제한 고려
                // 공식 문서에 따르면 API 속도 제한은 비활성화되었지만 여전히 상한선은 존재(초당 50건의 요청 범위 어딘가)하므로 안전하게 초당 25회로 제한
                Thread.sleep(40);

            } catch (Exception e) {
                log.error("TMDB 인기 영화 데이터 동기화 실패 - 페이지: {}", page, e);
            }
        }

        log.info("TMDB 인기 영화 데이터 동기화 완료");
    }

    /**
     * 3단계: 현재 상영중인 영화 데이터 동기화
     * 극장에서 현재 상영 중인 영화들을 별도로 표시하여 저장
     *
     * @param totalPages 동기화할 총 페이지 수
     */
    @Transactional
    public void syncNowPlayingMovies(int totalPages) {
        log.info("TMDB 현재 상영중인 영화 데이터 동기화 시작 - 페이지 수: {}", totalPages);

        for (int page = 1; page <= totalPages; page++) {
            try {
                String url = UriComponentsBuilder.fromUriString(baseUrl + "/movie/now_playing")
                        .queryParam("api_key", apiKey)
                        .queryParam("language", "ko-KR")
                        .queryParam("region", "KR")
                        .queryParam("page", page)
                        .toUriString();

                ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
                JsonNode rootNode = objectMapper.readTree(response.getBody());
                JsonNode moviesNode = rootNode.get("results");

                if (moviesNode != null && moviesNode.isArray()) {
                    for (JsonNode movieNode : moviesNode) {
                        processMovieData(movieNode, true);  // 현재 상영작으로 표시
                    }
                }

                Thread.sleep(40);

            } catch (Exception e) {
                log.error("TMDB 현재 상영중인 영화 데이터 동기화 실패 - 페이지: {}", page, e);
            }
        }

        log.info("TMDB 현재 상영중인 영화 데이터 동기화 완료");
    }

    /**
     * 4단계: 영화 상세 정보 및 출연진 정보 동기화
     * 이미 저장된 영화의 추가 정보(런타임, 출연진)를 가져옴
     *
     * @param movieId 로컬 DB의 영화 ID
     */
    @Transactional
    public void syncMovieDetailsAndCast(Long movieId) {
        log.info("영화 상세 정보 및 출연진 동기화 시작 - 영화 ID: {}", movieId);

        Movie movie = movieRepository.findById(movieId).orElse(null);
        if (movie == null || movie.getTmdbId() == null) {
            log.warn("영화를 찾을 수 없거나 TMDB ID가 없습니다 - 영화 ID: {}", movieId);
            return;
        }

        // 영화 상세 정보 조회
        syncMovieDetails(movie);

        // 출연진 정보 조회
        syncMovieCast(movie);
    }

    /**
     * 핵심 메서드: 개별 영화 데이터 처리
     * JSON 응답에서 영화 정보를 파싱하여 엔티티로 변환 후 저장
     *
     * 처리 과정:
     * 1. JSON에서 영화 기본 정보 추출 (제목, 줄거리, 개봉일 등)
     * 2. 날짜 형식 변환 및 예외 처리
     * 3. 기존 영화 찾기 또는 새 영화 생성
     * 4. 영화 속성 설정
     * 5. 장르 연결 (TMDB 장르 ID로 매핑)
     * 6. 데이터베이스 저장
     *
     * @param movieNode TMDB API 응답의 개별 영화 JSON 노드
     * @param isNowPlaying 현재 상영 중인 영화 여부
     */
    private void processMovieData(JsonNode movieNode, boolean isNowPlaying) {
        try {
            // 1. JSON에서 기본 정보 추출
            Long tmdbId = movieNode.get("id").asLong();
            String title = movieNode.get("title").asText();
            String overview = movieNode.has("overview") ? movieNode.get("overview").asText() : "";
            String releaseDateStr = movieNode.has("release_date") ? movieNode.get("release_date").asText() : null;
            Double popularity = movieNode.has("popularity") ? movieNode.get("popularity").asDouble() : 0.0;
            Double voteAverage = movieNode.has("vote_average") ? movieNode.get("vote_average").asDouble() : 0.0;
            Integer voteCount = movieNode.has("vote_count") ? movieNode.get("vote_count").asInt() : 0;
            String posterPath = movieNode.has("poster_path") && !movieNode.get("poster_path").isNull() ?
                    movieNode.get("poster_path").asText() : null;
            String backdropPath = movieNode.has("backdrop_path") && !movieNode.get("backdrop_path").isNull() ?
                    movieNode.get("backdrop_path").asText() : null;

            // 2. 개봉일 파싱 (YYYY-MM-DD 형식)
            LocalDate releaseDate = null;
            if (releaseDateStr != null && !releaseDateStr.isEmpty()) {
                try {
                    releaseDate = LocalDate.parse(releaseDateStr, DateTimeFormatter.ISO_LOCAL_DATE);
                } catch (Exception e) {
                    log.warn("날짜 파싱 실패: {}", releaseDateStr);
                }
            }

            // 3. 기존 영화 찾기 또는 새 영화 생성
            Movie movie = movieRepository.findByTmdbId(tmdbId).orElse(new Movie());

            // 4. 영화 속성 설정
            movie.setTmdbId(tmdbId);
            movie.setTitle(title);
            movie.setOverview(overview);
            movie.setReleaseDate(releaseDate);
            movie.setPopularity(popularity);
            movie.setVoteAverage(voteAverage);
            movie.setVoteCount(voteCount);
            movie.setIsNowPlaying(isNowPlaying);

            // 이미지 경로 설정 (null 체크)
            if (posterPath != null) {
                movie.setPosterPath(posterPath);
            }
            if (backdropPath != null) {
                movie.setBackdropPath(backdropPath);
            }

            // 5. 장르 연결 (TMDB 장르 ID 배열을 Genre 엔티티 리스트로 변환)
            JsonNode genreIdsNode = movieNode.get("genre_ids");
            if (genreIdsNode != null && genreIdsNode.isArray()) {
                List<Genre> genres = new ArrayList<>();
                for (JsonNode genreIdNode : genreIdsNode) {
                    Long genreId = genreIdNode.asLong();
                    genreRepository.findByTmdbId(genreId).ifPresent(genres::add);
                }
                movie.setGenres(genres);
            }

            // 6. 데이터베이스 저장
            movieRepository.save(movie);
            log.debug("영화 데이터 처리 완료: {}", title);

        } catch (Exception e) {
            log.error("영화 데이터 처리 중 오류 발생", e);
        }
    }

    /**
     * 영화 상세 정보 동기화
     *
     * @param movie 상세 정보를 가져올 영화 엔티티
     */
    private void syncMovieDetails(Movie movie) {
        try {
            String url = UriComponentsBuilder.fromUriString(baseUrl + "/movie/" + movie.getTmdbId())
                    .queryParam("api_key", apiKey)
                    .queryParam("language", "ko-KR")
                    .toUriString();

            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            JsonNode movieNode = objectMapper.readTree(response.getBody());

            // 런타임 정보 추가
            if (movieNode.has("runtime") && !movieNode.get("runtime").isNull()) {
                movie.setRuntime(movieNode.get("runtime").asInt());
            }

            movieRepository.save(movie);

        } catch (Exception e) {
            log.info("영화 상세 정보 동기화 실패 - TMDB ID: {}", movie.getTmdbId(), e);
        }
    }

    /**
     * 영화 출연진 정보 동기화
     * 주요 출연진 최대 10명의 정보를 가져와 저장
     *
     * 처리 과정:
     * 1. TMDB /movie/{id}/credits API 호출
     * 2. 출연진 배열에서 상위 10명 선택
     * 3. 각 배우의 TMDB ID로 기존 배우 찾기 또는 새로 생성
     * 4. 배우 정보 업데이트 및 저장
     * 5. 영화-배우 관계 설정
     *
     * @param movie 출연진 정보를 가져올 영화 엔티티
     */
    private void syncMovieCast(Movie movie) {
        try {
            String url = UriComponentsBuilder.fromUriString(baseUrl + "/movie/" + movie.getTmdbId() + "/credits")
                    .queryParam("api_key", apiKey)
                    .queryParam("language", "ko-KR")
                    .toUriString();

            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            JsonNode rootNode = objectMapper.readTree(response.getBody());
            JsonNode castNode = rootNode.get("cast");

            if (castNode != null && castNode.isArray()) {
                List<Actor> actors = new ArrayList<>();

                // 주요 출연진만 처리 (최대 10명)
                int count = 0;
                for (JsonNode actorNode : castNode) {
                    if (count >= 10) break;

                    Long tmdbId = actorNode.get("id").asLong();
                    String name = actorNode.get("name").asText();
                    String profilePath = actorNode.has("profile_path") && !actorNode.get("profile_path").isNull() ?
                            actorNode.get("profile_path").asText() : null;
                    Double popularity = actorNode.has("popularity") ? actorNode.get("popularity").asDouble() : 0.0;

                    // 기존 배우 찾기 또는 새 배우 생성
                    Actor actor = actorRepository.findByTmdbId(tmdbId).orElse(new Actor());
                    actor.setTmdbId(tmdbId);
                    actor.setName(name);
                    actor.setProfilePath(profilePath);
                    actor.setPopularity(popularity);

                    actorRepository.save(actor);
                    actors.add(actor);
                    count++;
                }

                // 영화-배우 관계 설정
                movie.setActors(actors);
                movieRepository.save(movie);
            }

        } catch (Exception e) {
            log.error("영화 출연진 정보 동기화 실패 - TMDB ID: {}", movie.getTmdbId(), e);
        }
    }

    /**
     * 전체 데이터 동기화 (초기 설정용)
     *
     * 실행 순서:
     * 1. 장르 동기화 (필수 선행 작업)
     * 2. 인기 영화 동기화 (10페이지 = 약 200개 영화)
     * 3. 현재 상영 영화 동기화 (5페이지 = 약 100개 영화)
     * 4. 모든 영화의 상세 정보 및 출연진 동기화
     */
    @Transactional
    public void syncAllData() {
        log.info("TMDB 전체 데이터 동기화 시작");

        // 1단계: 장르 동기화 (영화-장르 매핑을 위해 선행되어야 함)
        syncGenres();

        // 2단계: 인기 영화 동기화 (10페이지 = 최대 200개)
        syncPopularMovies(10);

        // 3단계: 현재 상영 중인 영화 동기화 (5페이지 = 최대 100개)
        syncNowPlayingMovies(5);

        // 4단계: 모든 영화의 추가 정보 동기화
        List<Movie> movies = movieRepository.findAll();
        for (Movie movie : movies) {
            if (movie.getTmdbId() != null) {
                syncMovieDetailsAndCast(movie.getId());

                try {
                    Thread.sleep(40); // API 호출 제한 고려
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }

        log.info("TMDB 전체 데이터 동기화 완료");
    }

    /**
     * TMDB 이미지 URL 생성 유틸리티
     *
     * @param imagePath TMDB에서 제공하는 이미지 경로
     * @param size 이미지 크기 (w300, w500, w780, original 등)
     * @return 완전한 이미지 URL
     */
    public String getImageUrl(String imagePath, String size) {
        if (imagePath == null || imagePath.isEmpty()) {
            return null;
        }
        return imageBaseUrl.replace("w500", size) + imagePath;
    }
}
