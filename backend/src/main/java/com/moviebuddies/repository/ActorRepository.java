package com.moviebuddies.repository;

import com.moviebuddies.entity.Actor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 *배우 데이터 접근 레포지토리
 * 배우 엔티티에 대한 CRUD, 검색, 그리고 통계 기능 제공
 * TMDB API와 연동하여 배우 정보를 관리하고, 배우별 출연작 분석 지원
 */
@Repository
public interface ActorRepository extends JpaRepository<Actor, Long> {

    /**
     * 배우명으로 배우 조회
     * 배우 검색이나 중복 확인에 사용
     *
     * @param name 배우 이름
     * @return 배우 정보 (필요시)
     */
    Optional<Actor> findByName(String name);

    /**
     * TMDB ID로 배우 조회
     * 외부 API(TMDB)와의 동기화 및 데이터 매핑에 사용
     *
     * @param tmdbId TMDB(The Movie Database) 배우 ID
     * @return 배우 정보 (Optional)
     */
    Optional<Actor> findByTmdbId(Long tmdbId);

    /**
     * 배우 이름 검색 (대소문자 구분 없음, 부분 일치)
     * 사용자가 배우를 검색할 때 사용되는 기본 검색 기능
     *
     * @param name 검색할 배우 이름 (부분 문자열)
     * @param pageable 페이징 정보
     * @return 검색된 배우 목록 (페이징)
     */
    @Query("SELECT a FROM Actor a WHERE LOWER(a.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    Page<Actor> findByNameContainingIgnoreCase(@Param("name") String name, Pageable pageable);

    /**
     * 인기도순 배우 조회
     * "인기 배우" 섹션이나 추천 배우 목록에서 사용
     *
     * @param pageable 페이징 정보
     * @return 모든 배우 목록 (인기도순)
     */
    Page<Actor> findAllByOrderByPopularityDesc(Pageable pageable);

    /**
     * 출연 영화 수가 많은 배우 순으로 조회
     * "다작 배우" 목록에서 사용
     * 많은 영화에 출연한 배우를 우선적으로 표시
     *
     * @param pageable 페이징 정보
     * @return 모든 배우 목록 (출연작 수 기준 내림차순)
     */
    @Query("SELECT a FROM Actor a LEFT JOIN a.movies m GROUP BY a ORDER BY COUNT(m) DESC")
    Page<Actor> findAllOrderByMovieCountDesc(Pageable pageable);

    /**
     * 특정 영화에 출연한 배우들 조회 (인기도순)
     * 영화 상세 페이지의 "출연진" 섹션에서 사용
     *
     * @param movieId 영화 ID
     * @return 해당 영화 출연 배우 목록 (인기도순)
     */
    @Query("SELECT a FROM Actor a JOIN a.movies m WHERE m.id = :movieId ORDER BY a.popularity DESC")
    List<Actor> findByMovieId(@Param("movieId") Long movieId);

    /**
     * 특정 장르 영화에 자주 출연하는 배우들 조회
     * 장르별 대표 배우 찾기에 사용
     * 해당 장르에서 많은 작품에 출연하고 인기도가 높은 배우 우선
     *
     * @param genreId 장르 ID
     * @param pageable 조회할 배우 수 제한
     * @return 해당 장르 전문 배우 목록 (출연작 수 및 인기도 기준)
     */
    @Query("SELECT DISTINCT a FROM Actor a " +
            "JOIN a.movies m " +
            "JOIN m.genres g " +
            "WHERE g.id = :genreId " +
            "GROUP BY a " +
            "ORDER BY COUNT(m) DESC, a.popularity DESC")
    List<Actor> findPopularActorsByGenre(@Param("genreId") Long genreId, Pageable pageable);

    /**
     * 배우별 출연 영화 수 통계 조회
     * 관리자 대시보드나 통계 페이지에서 사용
     * 각 배우의 출연작 개수를 함께 반환
     *
     * @return [배우명, 출연작수] 형태의 Object 배열 리스트 (출연작 수 기준 내림차순)
     */
    @Query("SELECT a.name, COUNT(m) FROM Actor a LEFT JOIN a.movies m GROUP BY a.id, a.name ORDER BY COUNT(m) DESC")
    List<Object[]> countMoviesByActor();

    /**
     * TMDB ID 존재 여부 확인
     * TMDB API에서 가져온 배우 데이터의 중복 방지에 사용
     *
     * @param tmdbId 확인할 TMDB 배우 ID
     * @return 존재하면 true
     */
    boolean existsByTmdbId(Long tmdbId);

    /**
     * 배우명 존재 여부 확인
     * 새로운 배우 등록 시 중복 검사에 사용
     *
     * @param name 확인할 배우명
     * @return 존재하면 true
     */
    boolean existsByName(String name);

    /**
     * 자동완성용 배우 이름 검색
     * 입력 키워드를 포함하는 배우 이름을 인기도 순으로 조회
     * 자동완성 드롭다운에 표시할 용도로 제한된 개수만 반환
     *
     * @param keyword 자동완성할 키워드
     * @param pageable 조회 개수 제한용 (보통 5개)
     * @return 키워드를 포함하는 인기 배우 목록
     */
    @Query("SELECT a FROM Actor a WHERE LOWER(a.name) LIKE LOWER(CONCAT('%', :keyword, '%')) ORDER BY a.popularity DESC")
    List<Actor> findTop5ByNameContainingIgnoreCase(@Param("keyword") String keyword, Pageable pageable);
}