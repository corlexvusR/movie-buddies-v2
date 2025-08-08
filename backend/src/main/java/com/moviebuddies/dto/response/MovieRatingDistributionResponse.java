package com.moviebuddies.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 영화별 평점 분포 응답 DTO
 * 영화 상세 페이지에서 평점 분포 시각화를 위해 사용
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MovieRatingDistributionResponse {

    /** 영화 ID */
    private Long movieId;

    /**
     * 평점별 리뷰 수 분포 데이터
     * Object[0]: 평점 (1~5)
     * Object[1]: 해당 평점의 리뷰 수
     * 예: [[1, 5], [2, 12], [3, 25], [4, 48], [5, 30]]
     */
    private List<Object[]> ratingDistribution;
}