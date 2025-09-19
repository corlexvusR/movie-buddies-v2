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
     * 전체 영화 제목순 조회 (오름차순)
     * 영화 목록을 가나다순(알파벳순)으로 정렬할 때 사용
     * 사용자가 제목순 정렬을 선택했을 때 호출됨
     *
     * @param pageable 페이징 정보
     * @return 제목 오름차순으로 정렬된 영화 목록
     */
    Page<Movie> findAllByOrderByTitleAsc(Pageable pageable);

    /**
     * 전체 영화 투표수순 조회 (내림차순)
     * 많은 사용자들이 평가한 영화부터 표시할 때 사용
     * TMDB의 vote_count 기준으로 정렬하여 평가 참여도가 높은 영화 우선 표시
     *
     * @param pageable 페이징 정보
     * @return 투표수 내림차순으로 정렬된 영화 목록
     */
    Page<Movie> findAllByOrderByVoteCountDesc(Pageable pageable);

    /**
     * 전체 영화 런타임순 조회 (오름차순)
     * 짧은 영화부터 긴 영화 순으로 정렬할 때 사용
     * 상영시간을 기준으로 정렬하여 시간 제약이 있는 사용자에게 유용
     *
     * @param pageable 페이징 정보
     * @return 런타임 오름차순으로 정렬된 영화 목록
     */
    Page<Movie> findAllByOrderByRuntimeAsc(Pageable pageable);

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
     * 복합 검색 (제목, 장르, 배우명, 개봉연도, 평점, 런타임, 현재상영으로 통합 검색)
     * 고급 검색 기능에서 여러 조건을 조합하여 검색할 때 사용
     * 각 파라미터는 null일 수 있으며, null인 경우 해당 조건 무시
     *
     * @param title 영화 제목 (부분 일치, null 가능)
     * @param genreId 장르 ID (null 가능)
     * @param actorName 배우 이름 (부분 일치, null 가능)
     * @param releaseYear 개봉 연도 (null 가능)
     * @param minRating 최소 평점 (null 가능)
     * @param maxRating 최대 평점 (null 가능)
     * @param minRuntime 최소 런타임 (null 가능)
     * @param maxRuntime 최대 런타임 (null 가능)
     * @param nowPlaying 현재 상영중 여부 (null 가능)
     * @param pageable 페이징 정보
     * @return 조건에 맞는 영화 목록 (인기순)
     */
    @Query("SELECT DISTINCT m FROM Movie m " +
            "LEFT JOIN m.genres g " +
            "LEFT JOIN m.actors a " +
            "WHERE (:title IS NULL OR LOWER(m.title) LIKE LOWER(CONCAT('%', :title, '%'))) " +
            "AND (:genreId IS NULL OR g.id = :genreId) " +
            "AND (:actorName IS NULL OR LOWER(a.name) LIKE LOWER(CONCAT('%', :actorName, '%'))) " +
            "AND (:releaseYear IS NULL OR YEAR(m.releaseDate) = :releaseYear) " +
            "AND (:minRating IS NULL OR m.voteAverage >= :minRating) " +
            "AND (:maxRating IS NULL OR m.voteAverage <= :maxRating) " +
            "AND (:minRuntime IS NULL OR m.runtime >= :minRuntime) " +
            "AND (:maxRuntime IS NULL OR m.runtime <= :maxRuntime) " +
            "AND (:nowPlaying IS NULL OR m.isNowPlaying = :nowPlaying) " +
            "ORDER BY m.popularity DESC")
    Page<Movie> searchMovies(@Param("title") String title,
                             @Param("genreId") Long genreId,
                             @Param("actorName") String actorName,
                             @Param("releaseYear") Integer releaseYear,
                             @Param("minRating") Double minRating,
                             @Param("maxRating") Double maxRating,
                             @Param("minRuntime") Integer minRuntime,
                             @Param("maxRuntime") Integer maxRuntime,
                             @Param("nowPlaying") Boolean nowPlaying,
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
     * 필터링용 복합 쿼리
     * 사이드바 필터 조건들을 조합하여 영화를 필터링
     * 각 파라미터는 null 가능하며, null인 경우 해당 조건 무시
     * LEFT JOIN을 사용하여 장르가 없는 영화도 포함
     *
     * @param genreIds 장르 ID 목록 (다중 선택 가능)
     * @param releaseYear 개봉 연도
     * @param minRating 최소 평점
     * @param maxRating 최대 평점
     * @param minRuntime 최소 런타임
     * @param maxRuntime 최대 런타임
     * @param nowPlaying 현재 상영중 여부
     * @param pageable 페이징 정보
     * @return 필터 조건에 맞는 영화 목록
     */
    @Query("SELECT DISTINCT m FROM Movie m " +
            "LEFT JOIN m.genres g " +
            "WHERE (:#{#genreIds == null || #genreIds.isEmpty()} = true OR g.id IN :genreIds) " +
            "AND (:releaseYear IS NULL OR YEAR(m.releaseDate) = :releaseYear) " +
            "AND (:minRating IS NULL OR m.voteAverage >= :minRating) " +
            "AND (:maxRating IS NULL OR m.voteAverage <= :maxRating) " +
            "AND (:minRuntime IS NULL OR m.runtime >= :minRuntime) " +
            "AND (:maxRuntime IS NULL OR m.runtime <= :maxRuntime) " +
            "AND (:nowPlaying IS NULL OR m.isNowPlaying = :nowPlaying)")
    Page<Movie> filterMovies(@Param("genreIds") List<Long> genreIds,
                             @Param("releaseYear") Integer releaseYear,
                             @Param("minRating") Double minRating,
                             @Param("maxRating") Double maxRating,
                             @Param("minRuntime") Integer minRuntime,
                             @Param("maxRuntime") Integer maxRuntime,
                             @Param("nowPlaying") Boolean nowPlaying,
                             Pageable pageable);

    /**
     * 제목 기준 검색 
     * 영화 제목에서 키워드를 검색하여 영화 조회
     * 영화 인기도순으로 정렬
     *
     * @param keyword 검색 키워드
     * @param pageable 페이징 정보
     * @return 인기도 기준으로 정렬된 영화 목록
     */
    @Query("SELECT DISTINCT m FROM Movie m " +
            "WHERE LOWER(m.title) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "ORDER BY m.popularity DESC")
    Page<Movie> searchByTitle(@Param("keyword") String keyword, Pageable pageable);

    /**
     * 배우 기준 검색
     * 배우 이름에서 키워드를 검색하여 해당 배우가 출연한 영화를 조회
     * 영화 인기도순으로 정렬
     *
     * @param keyword 검색 키워드 (배우 이름)
     * @param pageable 페이징 정보
     * @return 해당 배우 출연 영화 목록 (영화 인기도 순)
     */
    @Query("SELECT DISTINCT m FROM Movie m " +
            "JOIN m.actors a " +
            "WHERE LOWER(a.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "ORDER BY m.popularity DESC")
    Page<Movie> searchByActor(@Param("keyword") String keyword, Pageable pageable);

    /**
     * 제목과 배우 통합 검색 (관련도 정렬)
     * 영화 제목과 배우 이름을 모두 검색하여 영화 조회
     * 영화 인기도 순으로 정렬
     *
     * @param keyword 검색 키워드
     * @param pageable 페이징 정보
     * @return 통합 검색 결과 (인기도 기준 정렬)
     */
    @Query("SELECT DISTINCT m FROM Movie m " +
            "LEFT JOIN m.actors a " +
            "WHERE LOWER(m.title) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "   OR LOWER(a.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "ORDER BY m.popularity DESC")
    Page<Movie> searchByTitleAndActor(@Param("keyword") String keyword, Pageable pageable);

    /**
     * 자동완성용 영화 제목 검색
     * 입력 키워드를 포함하는 영화 제목을 인기도 순으로 조회
     * 자동완성 드롭다운에 표시할 용도로 제한된 개수만 반환
     *
     * @param keyword 자동완성할 키워드
     * @param pageable 조회 개수 제한용 (보통 5개)
     * @return 키워드를 포함하는 인기 영화 목록
     */
    @Query("SELECT m FROM Movie m WHERE LOWER(m.title) LIKE LOWER(CONCAT('%', :keyword, '%')) ORDER BY m.popularity DESC")
    List<Movie> findTop5ByTitleContainingIgnoreCase(@Param("keyword") String keyword, Pageable pageable);
}
