package com.moviebuddies.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 영화 필터링 요청 DTO
 * 영화 목록 페이지의 사이드바 필터 기능을 위한 데이터 전송 객체
 * 장르, 개봉연도, 평점, 런타임, 상영상태 등 다양한 조건으로 실시간 필터링 지원
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "영화 필터링 요청")
public class MovieFilterRequest {

    /**
     * 필터링할 장르 ID 목록
     * 다중 선택 가능하며, 선택된 장르 중 하나라도 포함하는 영화를 조회
     */
    @Schema(description = "장르 ID 목록", example = "[28, 12]")
    private List<Long> genreIds;

    /**
     * 필터링할 개봉 연도
     * 해당 연도에 개봉한 영화만 조회
     */
    @Schema(description = "개봉 연도", example = "2023")
    @Min(value = 1900, message = "개봉 연도는 1900년 이후여야 합니다.")
    @Max(value = 2026, message = "개봉 연도는 2026년 이전이어야 합니다.")
    private Integer releaseYear;

    /**
     * 최소 평점 (TMDB 기준)
     * 이 평점 이상의 영화만 조회
     */
    @Schema(description = "최소 평점", example = "7.0")
    @DecimalMin(value = "0.0", message = "최소 평점은 0.0 이상이어야 합니다.")
    @DecimalMax(value = "10.0", message = "최소 평점은 10.0 이하여야 합니다.")
    private Double minRating;

    /**
     * 최대 평점 (TMDB 기준)
     * 이 평점 이하의 영화만 조회
     */
    @Schema(description = "최대 평점", example = "10.0")
    @DecimalMin(value = "0.0", message = "최대 평점은 0.0 이상이어야 합니다.")
    @DecimalMax(value = "10.0", message = "최대 평점은 10.0 이하여야 합니다.")
    private Double maxRating;

    /**
     * 최소 런타임 (분)
     * 이 시간 이상의 영화만 조회
     */
    @Schema(description = "최소 런타임(분)", example = "90")
    @Min(value = 0, message = "최소 런타임은 0분 이상이어야 합니다.")
    private Integer minRuntime;

    /**
     * 최대 런타임 (분)
     * 이 시간 이하의 영화만 조회
     */
    @Schema(description = "최대 런타임(분)", example = "180")
    @Min(value = 0, message = "최대 런타임은 0분 이상이어야 합니다.")
    private Integer maxRuntime;

    /**
     * 현재 상영중 여부
     * true: 현재 상영중인 영화만, false: 상영 종료된 영화만, null: 전체
     */
    @Schema(description = "현재 상영중 여부", example = "true")
    private Boolean nowPlaying;

    /**
     * 필터 조건 존재 여부 확인
     * 하나라도 필터 조건이 설정되어 있는지 확인
     * 모든 조건이 null이면 전체 목록을 반환하기 위한 판단 기준
     *
     * @return 필터 조건이 하나라도 있으면 true, 모두 null이면 false
     */
    public boolean hasFilters() {
        return (genreIds != null && !genreIds.isEmpty()) ||
                releaseYear != null ||
                minRating != null ||
                maxRating != null ||
                minRuntime != null ||
                maxRuntime != null ||
                nowPlaying != null;
    }

    /**
     * 평점 범위 유효성 검증
     * 최소 평점이 최대 평점보다 큰 경우를 방지
     *
     * @return 평점 범위가 논리적으로 유효하면 true, 그렇지 않으면 false
     */
    public boolean isValidRatingRange() {
        if (minRating != null && maxRating != null) {
            return minRating <= maxRating;
        }
        return true;
    }

    /**
     * 런타임 범위 유효성 검증
     * 최소 런타임이 최대 런타임보다 큰 경우를 방지
     *
     * @return 런타임 범위가 논리적으로 유효하면 true, 그렇지 않으면 false
     */
    public boolean isValidRuntimeRange() {
        if (minRuntime != null && maxRuntime != null) {
            return minRuntime <= maxRuntime;
        }
        return true;
    }
}