package com.moviebuddies.service;

import com.moviebuddies.dto.response.GenreResponse;
import com.moviebuddies.entity.Genre;
import com.moviebuddies.exception.ResourceNotFoundException;
import com.moviebuddies.repository.GenreRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 장르 관련 비즈니스 로직 서비스
 * 장르 조회, 통계, 관리 기능을 제공
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GenreService {

    private final GenreRepository genreRepository;

    /**
     * 모든 장르 목록 조회
     * 영화 수가 많은 순으로 정렬하여 반환
     *
     * @return 모든 장르 목록 (영화 수 기준 내림차순)
     */
    public List<GenreResponse> getAllGenres() {
        log.info("모든 장르 목록 조회 요청");

        List<Genre> genres = genreRepository.findAllOrderByMovieCountDesc();

        log.info("총 {}개의 장르를 조회했습니다.", genres.size());

        return genres.stream()
                .map(GenreResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 특정 장르 정보 조회
     *
     * @param genreId 조회할 장르 ID
     * @return 장르 상세 정보
     * @throws ResourceNotFoundException 장르가 존재하지 않는 경우
     */
    public GenreResponse getGenre(Long genreId) {
        log.info("장르 상세 정보 조회 요청 - ID: {}", genreId);

        Genre genre = genreRepository.findById(genreId)
                .orElseThrow(() -> new ResourceNotFoundException("장르를 찾을 수 없습니다. ID: " + genreId));

        log.info("장르 조회 완료 - 이름: {}", genre.getName());

        return GenreResponse.from(genre);
    }

    /**
     * 장르명으로 장르 조회
     *
     * @param name 장르명
     * @return 장르 정보
     * @throws ResourceNotFoundException 장르가 존재하지 않는 경우
     */
    public GenreResponse getGenreByName(String name) {
        log.info("장르명으로 조회 요청 - 이름: {}", name);

        Genre genre = genreRepository.findByName(name)
                .orElseThrow(() -> new ResourceNotFoundException("장르를 찾을 수 없습니다. 이름: " + name));

        return GenreResponse.from(genre);
    }

    /**
     * TMDB ID로 장르 조회
     *
     * @param tmdbId TMDB 장르 ID
     * @return 장르 정보
     * @throws ResourceNotFoundException 장르가 존재하지 않는 경우
     */
    public GenreResponse getGenreByTmdbId(Long tmdbId) {
        log.info("TMDB ID로 장르 조회 요청 - TMDB ID: {}", tmdbId);

        Genre genre = genreRepository.findByTmdbId(tmdbId)
                .orElseThrow(() -> new ResourceNotFoundException("장르를 찾을 수 없습니다. TMDB ID: " + tmdbId));

        return GenreResponse.from(genre);
    }

    /**
     * 특정 영화에 속한 장르들 조회
     *
     * @param movieId 영화 ID
     * @return 해당 영화의 장르 목록
     */
    public List<GenreResponse> getGenresByMovieId(Long movieId) {
        log.info("영화별 장르 조회 요청 - 영화 ID: {}", movieId);

        List<Genre> genres = genreRepository.findByMovieId(movieId);

        log.info("영화 ID {}에 대한 {}개의 장르를 조회했습니다.", movieId, genres.size());

        return genres.stream()
                .map(GenreResponse::from)
                .collect(Collectors.toList());
    }
}
