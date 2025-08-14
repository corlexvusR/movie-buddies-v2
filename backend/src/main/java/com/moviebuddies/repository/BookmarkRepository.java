package com.moviebuddies.repository;

import com.moviebuddies.entity.Bookmark;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 북마크 엔티티 데이터 접근 계층(레포지토리)
 * 사용자의 영화 북마크 관련 데이터베이스 작업 담당
 */
@Repository
public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {

    /**
     * 사용자의 북마크 목록 조회 (페이징 지원)
     * 최신 북마크부터 정렬하여 반환
     *
     * @param userId 사용자 ID
     * @param pageable 페이징 정보
     * @return 페이징된 북마크 목록
     */
    Page<Bookmark> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    /**
     * 사용자의 북마크 목록 조회 (전체, 페이징 없음)
     * 사용자의 모든 북마크를 한 번에 조회할 때 사용
     *
     * @param userId 사용자 ID
     * @return 사용자의 전체 북마크 목록 (최신순)
     */
    List<Bookmark> findByUserIdOrderByCreatedAtDesc(Long userId);

    /**
     * 사용자의 특정 영화 북마크 조회
     * 북마크 존재 여부 확인 및 상세 정보 조회에 사용
     *
     * @param userId 사용자 ID
     * @param movieId 영화 ID
     * @return 북마크 정보 (존재하지 않으면 empty)
     */
    Optional<Bookmark> findByUserIdAndMovieId(Long userId, Long movieId);

    /**
     * 북마크 존재 여부 확인
     * 사용자가 특정 영화를 이미 북마크했는지 확인
     *
     * @param userId 사용자 ID
     * @param movieId 영화 ID
     * @return 북마크 존재 여부
     */
    boolean existsByUserIdAndMovieId(Long userId, Long movieId);

    /**
     * 사용자의 총 북마크 개수 조회
     * 사용자 프로필에 사용
     * 
     * @param userId 사용자 ID
     * @return 북마크 개수
     */
    long countByUserId(Long userId);

    /**
     * 특정 영화의 총 북마크 개수 조회
     * 영화 상세페이지에 인기도 지표로 표시
     *
     * @param movieId 영화 ID
     * @return 해당 영화의 북마크 개수
     */
    long countByMovieId(Long movieId);

    /**
     * 사용자가 북마크한 특정 장르의 영화 목록 조회
     * 장르별 북마크 필터링에 사용
     *
     * @param userId 사용자 ID
     * @param genreId 장르 ID
     * @return 해당 장르의 북마크 목록
     */
    @Query("SELECT b FROM Bookmark b JOIN b.movie.genres g WHERE b.user.id = :userId AND g.id = :genreId")
    List<Bookmark> findByUserIdAndGenreId(
            @Param("userId") Long userId, 
            @Param("genreId") Long genreId
    );

    /**
     * 가장 많이 북마크된 영화 순위 순위
     * 인기 영화 랭킹 또는 추천 시스템에 활용
     * 
     * @param pageable 페이징 정보
     * @return [영화 ID, 영화 제목, 북마크 수] 형태의 Object 배열 리스트
     */
    @Query("SELECT b.movie.id, b.movie.title, COUNT(b) as bookmarkCount FROM Bookmark b " + "GROUP BY b.movie.id, b.movie.title ORDER BY COUNT(b) DESC")
    List<Object[]> findMostBookmarkedMovies(Pageable pageable);
}
