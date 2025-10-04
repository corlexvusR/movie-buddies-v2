package com.moviebuddies.service;

import com.moviebuddies.dto.request.MovieFilterRequest;
import com.moviebuddies.dto.request.MovieSearchRequest;
import com.moviebuddies.dto.response.MovieAutocompleteResponse;
import com.moviebuddies.dto.response.MovieListResponse;
import com.moviebuddies.dto.response.MovieResponse;
import com.moviebuddies.entity.Actor;
import com.moviebuddies.entity.Genre;
import com.moviebuddies.entity.Movie;
import com.moviebuddies.exception.ResourceNotFoundException;
import com.moviebuddies.repository.ActorRepository;
import com.moviebuddies.repository.GenreRepository;
import com.moviebuddies.repository.MovieRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MovieService {

    private final MovieRepository movieRepository;
    private final GenreRepository genreRepository;
    private final ActorRepository actorRepository;

    /**
     * 영화 목록 조회 
     * 다양한 정렬 옵션 지원
     * 정렬 기준에 따라 적절한 Repository 메서드를 선택하여 호출
     * 캐시를 활용하여 동일한 조건의 반복 요청 시 성능 최적화
     *
     * @param pageable 페이징 정보 (페이지 번호, 크기 등)
     * @param sortBy 정렬 기준 (popularity, title, release_date, vote_average, vote_count, runtime)
     * @return 정렬된 영화 목록 (List 형태로 캐싱됨)
     */
    @Cacheable(value = "movies", key = "#pageable.pageNumber + '_' + #pageable.pageSize + '_' + #sortBy")
    public List<MovieListResponse> getMovieList(Pageable pageable, String sortBy) {

        // 정렬된 Pageable 생성
        Pageable sortedPageable = createSortedPageable(pageable, sortBy);

        // 정렬 기준에 따른 쿼리 실행
        Page<Movie> moviePage = switch (sortBy != null ? sortBy.toLowerCase() : "popularity") {
            case "title" -> movieRepository.findAllByOrderByTitleAsc(sortedPageable);
            case "release_date" -> movieRepository.findAllByOrderByReleaseDateDesc(sortedPageable);
            case "vote_average" -> movieRepository.findAllByOrderByVoteAverageDesc(sortedPageable);
            case "vote_count" -> movieRepository.findAllByOrderByVoteCountDesc(sortedPageable);
            case "runtime" -> movieRepository.findAllByOrderByRuntimeAsc(sortedPageable);
            default -> movieRepository.findAllByOrderByPopularityDesc(sortedPageable);
        };

        return moviePage.getContent().stream()
                .map(MovieListResponse::from)
                .collect(Collectors.toList());
    }

    public Page<MovieListResponse> getMovies(Pageable pageable, String sortBy) {
        // 캐시에서 List를 가져와서 Page로 감싸기
        List<MovieListResponse> movies = getMovieList(pageable, sortBy);
        long total = movieRepository.count(); // 전체 개수는 별도 조회
        return new PageImpl<>(movies, pageable, total);
    }

    /**
     * 영화 상세 정보 조회
     * 장르, 출연진, 리뷰 통계 등 모든 상세 정보 포함
     *
     * @param movieId 조회할 영화 ID
     * @return 영화 상세 정보
     * @throws ResourceNotFoundException 영화를 찾을 수 없는 경우
     */
//    @Cacheable(value = "movieDetail", key = "#movieId")
    public MovieResponse getMovieDetail(Long movieId) {
        log.info("영화 상세 정보 조회 - ID: {}", movieId);

        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new ResourceNotFoundException("영화", movieId));

        return MovieResponse.from(movie);
    }

    /**
     * 현재 상영중인 영화 TOP 5 조회
     * 인기도 기준으로 정렬
     *
     * @return 현재 상영중인 인기 영화 5편
     */
    @Cacheable(value = "nowPlayingTop5")
    public List<MovieListResponse> getNowPlayingTop5() {
        log.info("현재 상영중인 영화 TOP 5 조회");

        List<Movie> movies = movieRepository.findTop5ByIsNowPlayingTrueOrderByPopularityDesc();
        return movies.stream()
                .map(MovieListResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 장르별 인기 영화 TOP 5 조회
     *
     * @param genreId 장르 ID
     * @return 해당 장르의 인기 영화 5편
     * @throws ResourceNotFoundException 장르를 찾을 수 없는 경우
     */
    @Cacheable(value = "genreTop5", key = "#genreId")
    public List<MovieListResponse> getGenreTop5(Long genreId) {
        log.info("장르별 인기 영화 TOP 5 조회 - 장르 ID: {}", genreId);

        // 장르 존재 확인
        genreRepository.findById(genreId)
                .orElseThrow(() -> new ResourceNotFoundException("장르", genreId));

        Pageable top5 = PageRequest.of(0, 5);
        List<Movie> movies = movieRepository.findTop5ByGenreIdOrderByPopularityDesc(genreId, top5);

        return movies.stream()
                .map(MovieListResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 영화 검색 (네비게이션 검색용)
     * 키워드를 기반으로 영화 제목이나 배우 이름에서 검색
     *
     * @param searchRequest 검색 키워드와 검색 타입을 담은 요청 객체
     * @param pageable 페이징 정보
     * @return 검색 결과 영화 목록
     */
    public Page<MovieListResponse> searchMovies(MovieSearchRequest searchRequest, Pageable pageable) {
        log.info("영화 검색 - 키워드: {}, 타입: {}", searchRequest.getKeyword(), searchRequest.getSearchType());

        Page<Movie> movies = switch (searchRequest.getSearchType().toLowerCase()) {
            case "title" -> movieRepository.searchByTitle(searchRequest.getKeyword(), pageable);
            case "actor" -> movieRepository.searchByActor(searchRequest.getKeyword(), pageable);
            default -> movieRepository.searchByTitleAndActor(searchRequest.getKeyword(), pageable);
        };

        return movies.map(MovieListResponse::from);
    }

    /**
     * 장르별 영화 목록 조회
     * 평점 순으로 정렬
     *
     * @param genreId 장르 ID
     * @param pageable 페이징 정보
     * @return 해당 장르의 영화 목록
     * @throws ResourceNotFoundException 장르를 찾을 수 없는 경우
     */
    public Page<MovieListResponse> getMoviesByGenre(Long genreId, Pageable pageable) {
        log.info("장르별 영화 조회 - 장르 ID: {}", genreId);

        // 장르 존재 확인
        genreRepository.findById(genreId)
                .orElseThrow(() -> new ResourceNotFoundException("장르", genreId));

        Page<Movie> movies = movieRepository.findByGenreId(genreId, pageable);
        return movies.map(MovieListResponse::from);
    }

    /**
     * 배우별 출연 영화 조회
     *
     * @param actorId 배우 ID
     * @param pageable 페이징 정보
     * @return 해당 배우가 출연한 영화 목록
     * @throws ResourceNotFoundException 배우를 찾을 수 없는 경우
     */
    public Page<MovieListResponse> getMoviesByActor(Long actorId, Pageable pageable) {
        log.info("배우별 출연 영화 조회 - 배우 ID: {}", actorId);

        // 배우 존재 확인
        actorRepository.findById(actorId)
                .orElseThrow(() -> new ResourceNotFoundException("배우", actorId));

        Page<Movie> movies = movieRepository.findByActorId(actorId, pageable);
        return movies.map(MovieListResponse::from);
    }

    /**
     * 장르 기반 영화 추천
     * 기준 영화와 같은 장르의 다른 영화들을 추천
     *
     * @param movieId 기준 영화 ID
     * @return 추천 영화 목록 (최대 10편)
     * @throws ResourceNotFoundException 기준 영화를 찾을 수 없는 경우
     */
    @Cacheable(value = "recommendedMovies", key = "#movieId")
    public List<MovieListResponse> getRecommendedMovies(Long movieId) {
        log.info("영화 추천 조회 - 기준 영화 ID: {}", movieId);

        Movie baseMovie = movieRepository.findById(movieId)
                .orElseThrow(() -> new ResourceNotFoundException("영화", movieId));

        // 기준 영화의 장르 ID 목록 추출
        List<Long> genreIds = baseMovie.getGenres().stream()
                .map(Genre::getId)
                .collect(Collectors.toList());

        if (genreIds.isEmpty()) {
            return List.of();
        }

        PageRequest top10 = PageRequest.of(0, 10);
        List<Movie> recommendedMovies = movieRepository.findRecommendedMoviesByGenres(genreIds, movieId, top10);

        return recommendedMovies.stream()
                .map(MovieListResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 연도별 영화 조회
     *
     * @param year 개봉 연도
     * @param pageable 페이징 정보
     * @return 해당 연도 개봉 영화 목록
     */
    public Page<MovieListResponse> getMoviesByYear(Integer year, Pageable pageable) {
        log.info("연도별 영화 조회 - 연도: {}", year);

        Page<Movie> movies = movieRepository.findByReleaseYear(year, pageable);
        return movies.map(MovieListResponse::from);
    }

    /**
     * 평점 범위로 영화 조회
     *
     * @param minRating 최소 평점
     * @param maxRating 최대 평점
     * @param pageable 페이징 정보
     * @return 해당 평점 범위의 영화 목록
     */
    public Page<MovieListResponse> getMoviesByRating(Double minRating, Double maxRating, Pageable pageable) {
        log.info("평점 범위 영화 조회 - 범위: {}분 ~ {}분", minRating, maxRating);

        Page<Movie> movies = movieRepository.findByVoteAverageBetween(minRating, maxRating, pageable);
        return movies.map(MovieListResponse::from);
    }

    /**
     * 런타임 범위로 영화 조회
     *
     * @param minRuntime 최소 런타임 (분)
     * @param maxRuntime 최대 런타임 (분)
     * @param pageable 페이징 정보
     * @return 해당 런타임 범위의 영화 목록
     */
    public Page<MovieListResponse> getMoviesByRuntime(Integer minRuntime, Integer maxRuntime, Pageable pageable) {
        log.info("런타임 범위 영화 조회 - 범위: {}분 ~ {}분", minRuntime, maxRuntime);

        Page<Movie> movies = movieRepository.findByRuntimeBetween(minRuntime, maxRuntime, pageable);
        return movies.map(MovieListResponse::from);
    }

    /**
     * 정렬 기준에 따른 Pageable 객체 생성
     *
     * @param pageable 기본 페이징 정보
     * @param sortBy 정렬 기준
     * @return 정렬이 적용된 Pageable 객체
     */
    public Pageable createSortedPageable(Pageable pageable, String sortBy) {
        Sort sort = switch (sortBy != null ? sortBy.toLowerCase() : "popularity") {
            case "title" -> Sort.by(Sort.Direction.ASC, "title");
            case "release_date" -> Sort.by(Sort.Direction.DESC, "releaseDate");
            case "vote_average" -> Sort.by(Sort.Direction.DESC, "voteAverage");
            case "vote_count" -> Sort.by(Sort.Direction.DESC, "voteCount");
            case "runtime" -> Sort.by(Sort.Direction.ASC, "runtime");
            default -> Sort.by(Sort.Direction.DESC, "popularity");
        };

        return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);
    }

    /**
     * 영화 필터링 (사이드바 필터용)
     * 여러 조건을 조합하여 영화를 필터링하는 기능
     * 각 조건은 선택사항이며, null인 경우 해당 조건은 무시됨
     *
     * @param filterRequest 필터링 조건들을 담은 요청 객체
     * @param pageable 페이징 정보
     * @return 필터 조건에 맞는 영화 목록
     */
    public Page<MovieListResponse> filterMovies(MovieFilterRequest filterRequest, Pageable pageable) {
        log.info("영화 필터링 - 조건: {}", filterRequest);

        Page<Movie> movies = movieRepository.filterMovies(
                filterRequest.getGenreIds(),
                filterRequest.getReleaseYear(),
                filterRequest.getMinRating(),
                filterRequest.getMaxRating(),
                filterRequest.getMinRuntime(),
                filterRequest.getMaxRuntime(),
                filterRequest.getNowPlaying(),
                pageable
        );

        return movies.map(MovieListResponse::from);
    }

    /**
     * 검색어 자동완성 제안
     * 사용자가 입력한 키워드를 포함하는 영화와 배우를 각각 최대 5개씩 제안
     * 인기도 순으로 정렬하여 가장 관련성 높은 결과를 우선 표시
     *
     * @param keyword 자동완성할 검색 키워드
     * @return 추천 영화와 배우 목록이 포함된 자동완성 응답
     */
    public MovieAutocompleteResponse getAutocomplete(String keyword) {
        Pageable top5 = PageRequest.of(0, 5);

        List<Movie> movies = movieRepository.findTop5ByTitleContainingIgnoreCase(keyword, top5);
        List<Actor> actors = actorRepository.findTop5ByNameContainingIgnoreCase(keyword, top5);

        List<MovieAutocompleteResponse.MovieSuggestion> movieSuggestions = movies.stream()
                .map(movie -> MovieAutocompleteResponse.MovieSuggestion.builder()
                        .id(movie.getId())
                        .title(movie.getTitle())
                        .posterImageUrl(movie.getPosterImageUrl())
                        .releaseYear(movie.getReleaseDate() != null ? movie.getReleaseDate().getYear() : null)
                        .build())
                .collect(Collectors.toList());

        List<MovieAutocompleteResponse.ActorSuggestion> actorSuggestions = actors.stream()
                .map(actor -> MovieAutocompleteResponse.ActorSuggestion.builder()
                        .id(actor.getId())
                        .name(actor.getName())
                        .profileImageUrl(actor.getProfileImageUrl())
                        .build())
                .collect(Collectors.toList());

        return MovieAutocompleteResponse.builder()
                .movies(movieSuggestions)
                .actors(actorSuggestions)
                .build();
    }
}
