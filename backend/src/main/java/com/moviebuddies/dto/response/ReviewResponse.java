package com.moviebuddies.dto.response;

import com.moviebuddies.entity.Review;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 리뷰 정보 응답 DTO
 * 리뷰 목록 조회나 상세 조회 시 리뷰 정보와 관련된 영화/사용자 정보를 함께 전달
 * 리뷰 작성자의 프로필 정보와 리뷰가 작성된 영화의 기본 정보 포함
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewResponse {

    /**
     * 리뷰 고유 ID
     */
    private Long id;

    /**
     * 리뷰가 작성된 영화 ID
     */
    private Long movieId;

    /**
     * 리뷰가 작성된 영화 제목
     */
    private String movieTitle;

    /**
     * 영화 포스터 이미지 URL
     */
    private String moviePosterUrl;

    /**
     * 리뷰 작성자 사용자 ID
     */
    private Long userId;

    /**
     * 리뷰 작성자 사용자명
     */
    private String username;

    /**
     * 리뷰 작성자 닉네임
     */
    private String userNickname;

    /**
     * 리뷰 작성자 프로필 이미지 URL
     */
    private String userProfileImageUrl;

    /**
     * 리뷰 내용
     */
    private String content;

    /**
     * 평점 (1점 - 5점)
     */
    private Integer rating;

    /**
     * 리뷰 작성 시간
     */
    private LocalDateTime createdAt;

    /**
     * 리뷰 마지막 수정 시간
     */
    private LocalDateTime lastModifiedAt;

    /**
     * Review 엔티티를 ReviewResponse DTO로 변환
     * 연관된 영화 정보와 사용자 정보를 함께 반환하여 포함
     * 
     * @param review 변환할 Review 엔티티
     * @return ReviewResponse DTO 객체
     */
    public static ReviewResponse from(Review review) {
        return ReviewResponse.builder()
                .id(review.getId())
                .movieId(review.getMovie().getId())
                .movieTitle(review.getMovie().getTitle())
                .moviePosterUrl(review.getMovie().getPosterImageUrl())
                .userId(review.getUser().getId())
                .username(review.getUser().getUsername())
                .userNickname(review.getUser().getNickname())
                .userProfileImageUrl(review.getUser().getProfileImageUrl())
                .content(review.getContent())
                .rating(review.getRating())
                .createdAt(review.getCreatedAt())
                .lastModifiedAt(review.getLastModifiedAt())
                .build();
    }
}
