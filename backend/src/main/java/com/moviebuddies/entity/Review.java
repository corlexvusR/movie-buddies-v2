package com.moviebuddies.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 리뷰 엔티티
 * 사용자가 영화에 대해 작성하는 리뷰와 평점을 관리
 * 한 사용자는 한 영화에 대해 하나의 리뷰만 작성 가능
 */
@Entity
@Table(name = "reviews", uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "movie_id"}))
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class Review {

    /**
     * 리뷰 고유 식별자 (Primary Key)
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 리뷰를 작성한 사용자
     * 지연 로딩으로 성능 최적화
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * 리뷰 대상 영화
     * 지연 로딩으로 성능 최적화
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "movie_id", nullable = false)
    private Movie movie;

    /**
     * 리뷰 내용
     * 최대 500자까지 작성 가능
     */
    @Column(nullable = false, length = 500)
    private String content;

    /**
     * 영화 평점
     * 최대 500자까지 작성 가능
     */
    @Column(nullable = false)
    private Integer rating; // 1점에서 5점 사이

    /**
     * 리뷰 작성 시간
     * JPA Auditing
     */
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 리뷰 마지막 수정 시간
     * JPA Auditing으로 자동 업데이트
     */
    @LastModifiedDate
    @Column(name = "last_modified_at")
    private LocalDateTime lastModifiedAt;

    /**
     * 리뷰 내용 및 평점 업데이트
     * null이 아닌 값들만 업데이트하며, 평점은 유효성 검사 실시
     *
     * @param content 새로운 리뷰 내용
     * @param rating 새로운 평점 (1에서 5 사이의 값)
     */
    public void updateReview(String content, Integer rating) {
        if (content != null && !content.trim().isEmpty()) {
            this.content = content.trim();
        }
        if (rating != null && rating >= 1 && rating <= 5) {
            this.rating = rating;
        }
    }

    /**
     * 평점 유효성 검사
     * 평점이 1-5 범위를 벗어나면 예외 발생
     *
     * @throws IllegalArgumentException 평점이 유효 범위를 벗어난 경우
     */
    public void validateRating() {
        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException("평점은 1점에서 5점 사이여야 합니다.");
        }
    }
}
