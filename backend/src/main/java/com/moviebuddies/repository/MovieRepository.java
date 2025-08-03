package com.moviebuddies.repository;

import com.moviebuddies.entity.Movie;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * 영화 데이터 접근 레포지토리
 * 영화 엔티티에 대한 기본 CRUD 및 다양한 검색/조회 기능 제공
 * TMDB API와 연동하여 영화 정보를 관리하고, 복합 검색 및 추천 기능 지원
 */
@Repository
public interface MovieRepository extends JpaRepository<Movie, Long> {

    /**
     * TMDB ID로 영화 조회
     * 외부 API(TMDB)와의 동기화 및 중복 방지에 사용
     *
     * @param tmdbId TMDB 고유 ID
     * @return 영화 정보 (필요시)
     */
    Optional<Movie> findByTmdbId(Long tmdbId);

    /**
     * 제목으로 영화 검색 (대소문자 구분 없음, 부분 일치)
     * 사용자가 영화 검색 시 사용되는 기본 검색 기능
     *
     * @param title 검색할 영화 제목 (부분 문자열)
     * @param pageable 페이징 정보
     * @return 검색된 영화 목록 (페이징)
     */
    @Query("SELECT m FROM Movie m WHERE LOWER(m.title) LIKE LOWER(CONCAT('%', :title, '%'))")
    Page<Movie> findByTitleContainingIgnoreCase(@Param("title") String title, Pageable pageable);

    /**
     * 현재 상영중인 영화 조회 (인기순 정렬)
     * 메인 페이지의 "현재 상영작" 섹션에서 사용
     *
     * @param pageable 페이징 정보
     * @return 현재 상영중인 영화 목록 (인기순)
     */
    Page<Movie> findByIsNowPlayingTrueOrderByPopularityDesc(Pageable pageable);

    /**
     * 전체 영화 인기순 조회
     * "인기 영화" 섹션 및 기본 정렬에서 사용
     *
     * @param pageable 페이징 정보
     * @return 모든 영화 목록 (인기순)
     */
    Page<Movie> findAllByOrderByPopularityDesc(Pageable pageable);

    /**
     * 전체 영화 평점순 조회
     * "평점 높은 영화" 필터링에서 사용
     *
     * @param pageable 페이징 정보
     * @return 모든 영화 목록 (평점순)
     */
    Page<Movie> findAllByOrderByVoteAverageDesc(Pageable pageable);

    /**
     * 최신 영화 조회 (개봉일 기준)
     * "최신 개봉작" 섹션에서 사용
     *
     * @param pageable 페이징 정보
     * @return 모든 영화 목록 (최신순)
     */
    Page<Movie> findAllByOrderByReleaseDateDesc(Pageable pageable);

    /**
     * 특정 장르의 영화 조회 (평점순 정렬)
     * 장르별 영화 필터링 기능에서 사용
     *
     * @param genreId 장르 ID
     * @param pageable 페이징 정보
     * @return 해당 장르의 영화 목록 (평점순)
     */
    @Query("SELECT m FROM Movie m JOIN m.genres g WHERE g.id = :genreId ORDER BY m.voteAverage DESC")
    Page<Movie> findByGenreIdOrderByVoteAverageDesc(@Param("genreId") Long genreId, Pageable pageable);

    /**
     * 특정 장르의 인기 영화 TOP N
     * 장르별 추천 영화나 미리보기에서 사용
     *
     * @param genreId 장르 ID
     * @param pageable 조회할 개수 제한용 (Top 5 등)
     * @return 해당 장르의 인기 영화 목록 (제한된 개수)
     */
    @Query("SELECT m FROM Movie m JOIN m.genres g WHERE g.id = :genreId ORDER BY m.popularity DESC")
    List<Movie> findTop5ByGenreIdOrderByPopularityDesc(@Param("genreId") Long genreId, Pageable pageable);

    /**
     * 현재 상영중인 인기 영화 TOP 5
     * 메인 페이지 상단 배너나 추천 섹션에서 사용
     *
     * @return 현재 상영중인 인기 영화 상위 5개
     */
    List<Movie> findTop5ByIsNowPlayingTrueOrderByPopularityDesc();

    /**
     * 특정 배우가 출연한 영화 조회 (최신순)
     * 배우 상세 페이지의 "출연 작품" 섹션에서 사용
     *
     * @param actorId 배우 ID
     * @param pageable 페이징 정보
     * @return 해당 배우 출연 영화 목록 (최신순)
     */
    @Query("SELECT m FROM Movie m JOIN m.actors a WHERE a.id = :actorId ORDER BY m.releaseDate DESC")
    Page<Movie> findByActorId(@Param("actorId") Long actorId, Pageable pageable);

    /**
     * 개봉 연도별 영화 조회 (인기순)
     * 연도별 영화 필터링 기능에서 사용
     *
     * @param year 개봉 연도
     * @param pageable 페이징 정보
     * @return 해당 연도 개봉 영화 목록 (인기순)
     */
    @Query("SELECT m FROM Movie m WHERE YEAR(m.releaseDate) = :year ORDER BY m.popularity DESC")
    Page<Movie> findByReleaseYear(@Param("year") Integer year, Pageable pageable);

    /**
     * 특정 기간의 영화 조회 (개봉일 기준)
     * 기간별 영화 검색이나 특별 기획전에서 사용
     *
     * @param startDate 시작 날짜
     * @param endDate 종료 날짜
     * @param pageable 페이징 정보
     * @return 해당 기간 개봉 영화 목록 (최신순)
     */
    @Query("SELECT m FROM Movie m WHERE m.releaseDate BETWEEN :startDate AND :endDate ORDER BY m.releaseDate DESC")
    Page<Movie> findByReleaseDateBetween(@Param("startDate") LocalDate startDate,
                                         @Param("endDate") LocalDate endDate,
                                         Pageable pageable);

    /**
     * 런타임 범위로 영화 조회 (평점순)
     * "짧은 영화", "긴 영화" 등의 필터링에서 사용
     *
     * @param minRuntime 최소 런타임 (분)
     * @param maxRuntime 최대 런타임 (분)
     * @param pageable 페이징 정보
     * @return 해당 런타임 범위의 영화 목록 (평점순)
     */
    @Query("SELECT m FROM Movie m WHERE m.runtime BETWEEN :minRuntime AND :maxRuntime ORDER BY m.voteAverage DESC")
    Page<Movie> findByRuntimeBetween(@Param("minRuntime") Integer minRuntime,
                                     @Param("maxRuntime") Integer maxRuntime,
                                     Pageable pageable);

    /**
     * 평점 범위로 영화 조회 (평점순)
     * 평점 필터링 기능에서 사용 (예: 5점 이상 영화만)
     *
     * @param minRating 최소 평점
     * @param maxRating 최대 평점
     * @param pageable 페이징 정보
     * @return 해당 평점 범위의 영화 목록 (평점순)
     */
    @Query("SELECT m FROM Movie m WHERE m.voteAverage BETWEEN :minRating AND :maxRating ORDER BY m.voteAverage DESC")
    Page<Movie> findByVoteAverageBetween(@Param("minRating") Double minRating,
                                         @Param("maxRating") Double maxRating,
                                         Pageable pageable);

    /**
     * 복합 검색 (제목, 장르, 배우명으로 통합 검색)
     * 고급 검색 기능에서 여러 조건을 조합하여 검색할 때 사용
     * 각 파라미터는 null일 수 있으며, null인 경우 해당 조건 무시
     *
     * @param title 영화 제목 (부분 일치, null 가능)
     * @param genreId 장르 ID (null 가능)
     * @param actorName 배우 이름 (부분 일치, null 가능)
     * @param pageable 페이징 정보
     * @return 조건에 맞는 영화 목록 (인기순)
     */
    @Query("SELECT DISTINCT m FROM Movie m " +
            "LEFT JOIN m.genres g " +
            "LEFT JOIN m.actors a " +
            "WHERE (:title IS NULL OR LOWER(m.title) LIKE LOWER(CONCAT('%', :title, '%'))) " +
            "AND (:genreId IS NULL OR g.id = :genreId) " +
            "AND (:actorName IS NULL OR LOWER(a.name) LIKE LOWER(CONCAT('%', :actorName, '%'))) " +
            "ORDER BY m.popularity DESC")
    Page<Movie> searchMovies(@Param("title") String title,
                             @Param("genreId") Long genreId,
                             @Param("actorName") String actorName,
                             Pageable pageable);

    /**
     * 추천 영화 조회 (유사한 장르 기반)
     * 영화 상세 페이지의 "비슷한 영화" 추천에서 사용
     * 현재 영화와 같은 장르의 다른 영화들을 평점 및 인기도순으로 추천
     *
     * @param genreIds 추천 기준이 되는 장르 ID 목록
     * @param excludeMovieId 제외할 영화 ID (현재 보고 있는 영화)
     * @param pageable 추천할 영화 개수 제한
     * @return 추천 영화 목록 (평점 및 인기도순)
     */
    @Query("SELECT DISTINCT m FROM Movie m " +
            "JOIN m.genres g " +
            "WHERE g.id IN :genreIds " +
            "AND m.id != :excludeMovieId " +
            "ORDER BY m.voteAverage DESC, m.popularity DESC")
    List<Movie> findRecommendedMoviesByGenres(@Param("genreIds") List<Long> genreIds,
                                              @Param("excludeMovieId") Long excludeMovieId,
                                              Pageable pageable);

    /**
     * 현재 상영중인 영화 수 조회
     * 대시보드나 통계 페이지에서 사용
     *
     * @return 현재 상영중인 영화 개수
     */
    @Query("SELECT COUNT(m) FROM Movie m WHERE m.isNowPlaying = true")
    Long countNowPlayingMovies();

    /**
     * 특정 연도 개봉 영화 수 조회
     * 연도별 통계나 연간 리포트에서 사용
     *
     * @param year 조회할 연도
     * @return 해당 연도 개봉 영화 개수
     */
    @Query("SELECT COUNT(m) FROM Movie m WHERE YEAR(m.releaseDate) = :year")
    Long countMoviesByYear(@Param("year") Integer year);

    /**
     * 전체 영화 평균 평점 조회
     * 전체 데이터베이스 품질 지표나 통계에서 사용
     *
     * @return 모든 영화의 평균 평점
     */
    @Query("SELECT AVG(m.voteAverage) FROM Movie m")
    Double getAverageRating();

    /**
     * 최근 추가된 영화 조회 (생성일 기준)
     * 관리자 페이지나 "최근 추가된 영화" 섹션에서 사용
     *
     * @param pageable 조회할 개수 제한
     * @return 최근 추가된 영화 목록 (추가 순서 역순)
     */
    @Query("SELECT m FROM Movie m ORDER BY m.createdAt DESC")
    List<Movie> findRecentlyAddedMovies(Pageable pageable);
}
