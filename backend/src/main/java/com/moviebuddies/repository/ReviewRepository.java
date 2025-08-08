package com.moviebuddies.repository;

import com.moviebuddies.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 리뷰 데이터 접근 레포지토리
 * 영화 리뷰 관련 데이터베이스 작업 담당
 */
@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    /**
     * 특정 영화의 리뷰를 최신순으로 페이징 조회
     *
     * @param movieId 영화 ID
     * @param pageable 페이징 정보
     * @return 영화의 리뷰 목록 (페이징)
     */
    Page<Review> findByMovieIdOrderByCreatedAtDesc(Long movieId, Pageable pageable);

    /**
     * 특정 사용자의 리뷰를 최신 순으로 페이징 조회
     *
     * @param userId 사용자 ID
     * @param pageable 페이징 정보
     * @return 사용자의 리뷰 목록 (페이징)
     */
    Page<Review> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    /**
     * 특정 사용자의 특정 영화에 대한 리뷰 조회
     *
     * @param userId 사용자 ID
     * @param movieId 영화 ID
     * @return 리뷰 정보 (Optional)
     */
    Optional<Review> findByUserIdAndMovieId(Long userId, Long movieId);

    /**
     * 중복 리뷰 작성 방지를 위한 존재 여부 확인
     *
     * @param userId 사용자 ID
     * @param movieId 영화 ID
     * @return 리뷰 존재 여부
     */
    boolean existsByUserIdAndMovieId(Long userId, Long movieId);

    /**
     * 특정 영화의 총 리뷰 개수 조회
     *
     * @param movieId 영화 ID
     * @return 리뷰 개수
     */
    long countByMovieId(Long movieId);

    /**
     * 특정 사용자의 총 리뷰 개수 조회
     *
     * @param userId 사용자 ID
     * @return 리뷰 개수
     */
    long countByUserId(Long userId);

    /**
     * 특정 영화의 평균 평점 계산
     *
     * @param movieId 영화 ID
     * @return 평균 평점
     */
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.movie.id = :movieId")
    Double getAverageRatingByMovieId(@Param("movieId") Long movieId);

    /**
     * 특정 영화의 평점별 리뷰 수 분포 조회
     *
     * @param movieId 영화 ID
     * @return 평점별 리뷰 수 배열 리스트 [평점, 개수]
     */
    @Query("SELECT r.rating, COUNT(r) FROM Review r WHERE r.movie.id = :movieId GROUP BY r.rating ORDER BY r.rating")
    List<Object[]> getRatingDistributionByMovie(@Param("movieId") Long movieId);
}