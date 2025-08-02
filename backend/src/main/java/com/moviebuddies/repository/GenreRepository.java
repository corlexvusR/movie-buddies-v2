package com.moviebuddies.repository;

import com.moviebuddies.entity.Genre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 장르 데이터 접근 레포지토리
 * 영화 장르 정보에 대한 CRUD 및 통계 기능 제공
 * TMDB API와 연동하여 장르 정보를 관리하고 장르별 영화 통계 지원
 */
@Repository
public interface GenreRepository extends JpaRepository<Genre, Long> {

    /**
     * 장르명으로 장르 조회
     * 장르 검색이나 중복 확인에 사용
     * 
     * @param name 장르명
     * @return 장르 정보 (필요시)
     */
    Optional<Genre> findByName(String name);

    /**
     * TMDB ID로 장르 조회
     * 외부 API(TMDB)와의 동기화 및 데이터 매핑에 사용
     *
     * @param tmdbId TMDB 장르 ID
     * @return 장르 정보 (필요시)
     */
    Optional<Genre> findByTmdbId(Long tmdbId);

    /**
     * 장르명 존재 여부 확인
     * 새로운 장르 등록 시 중복 검사에 사용
     *
     * @param name 확인할 장르명
     * @return 존재하면 true
     */
    boolean existsByName(String name);

    /**
     * TMDB 장르 ID 존재 여부 확인
     * TMDB API에서 가져온 장르 데이터의 중복 방지에 사용
     *
     * @param tmdbId 확인할 TMDB 장르 ID
     * @return 존재하면 true
     */
    boolean existsByTmdbId(Long tmdbId);

    /**
     * 영화 수가 많은 장르 순으로 조회
     * 인기 장르 표시나 장르 목록 정렬에 사용
     * 영화가 많이 등록된 장르를 우선적으로 보여줌
     *
     * @return 모든 장르 목록 (보유 영화 수 기준 내림차순)
     */
    @Query("SELECT g FROM Genre g LEFT JOIN g.movies m GROUP BY g ORDER BY COUNT(m) DESC")
    List<Genre> findAllOrderByMovieCountDesc();

    /**
     * 장르별 영화 수 통계 조회
     * 관리자 대시보드나 통계 페이지에서 사용
     * 각 장르가 보유한 영화 개수를 함께 반환
     *
     * @return [장르명, 영화 수] 형태의 Object 배열 리스트
     */
    @Query("SELECT g.name, COUNT(m) FROM Genre g LEFT JOIN g.movies m GROUP BY g.id, g.name")
    List<Object[]> countMoviesByGenre();

    /**
     * 특정 영화에 할당된 장르들 조회
     * 영화 상세 페이지에서 해당 영화의 장르 정보 표시에 사용
     * 영화 수정 시 기존 장르 정보 로드에도 활용
     *
     * @param movieId 영화 ID
     * @return 해당 영화에 속한 장르 목록
     */
    @Query("SELECT g FROM Genre g JOIN g.movies m WHERE m.id = :movieId")
    List<Genre> findByMovieId(@Param("movieId") Long movieId);
}