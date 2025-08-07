package com.moviebuddies.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 리뷰 작성/수정 요청 DTO
 * 사용자가 영화에 대한 리뷰와 평점을 작성할 때 사용되는 데이터 전송 객체
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReviewRequest {

    /**
     * 리뷰를 작성할 영화의 고유 ID
     */
    @NotNull(message = "영화 ID는 필수입니다.")
    private Long movieId;

    /**
     * 리뷰 내용
     */
    @NotNull(message = "리뷰 내용은 필수입니다.")
    private String content;

    /**
     * 영화 평점
     */
    @NotNull(message = "평점은 필수입니다.")
    @Min(value = 1, message = "평점은 최소 1점입니다.")
    @Max(value = 5, message = "평점은 최대 5점입니다.")
    private Integer rating;
}
