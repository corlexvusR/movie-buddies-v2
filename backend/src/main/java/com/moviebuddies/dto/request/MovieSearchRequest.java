package com.moviebuddies.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 영화 검색 요청 DTO
 *
 * 클라이언트에서 전송하는 영화 검색 조건들을 담는 데이터 전송 객체
 * 제목, 장르, 배우, 개봉연도, 평점, 런타임, 현재상영 등 다양한 조건으로 검색 가능
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "영화 검색 요청")
public class MovieSearchRequest {

    /**
     * 검색할 영화 제목
     * 부분 일치 검색 지원 (LIKE 쿼리 사용)
     */
    @Schema(description = "영화 제목 (부분 검색)", example = "슈퍼맨")
    @Size(max = 200, message = "제목은 200자 이하여야 합니다.")
    private String title;

    /**
     * 필터링할 장르 ID
     * Genre 테이블의 PK 값
     */
    @Schema(description = "장르 ID", example = "28")
    private Long genreId;

    /**
     * 검색할 배우 이름
     * 부분 일치 검색 지원 (LIKE 쿼리 사용)
     */
    @Schema(description = "배우 이름 (부분 검색)", example = "데이비드 코렌스웻")
    @Size(max = 100, message = "배우명은 100자 이하여야 합니다.")
    private String actorName;

    /**
     * 필터링할 개봉 연도
     * 해당 연도에 개봉한 영화만 조회
     */
    @Schema(description = "개봉 연도", example = "2025")
    @Min(value = 1900, message = "개봉 연도는 1900년 이후여야 합니다.")
    @Max(value = 2026, message = "개봉 연도는 2026년 이전이어야 합니다.")
    private Integer releaseYear;

    /**
     * 최소 평점
     * 0.0 ~ 10.0 범위의 TMDB 평점
     */
    @Schema(description = "최소 평점", example = "7.0")
    @DecimalMin(value = "0.0", message = "최소 평점은 0.0 이상이어야 합니다.")
    @DecimalMax(value = "10.0", message = "최소 평점은 10.0 이하여야 합니다.")
    private Double minRating;

    /**
     * 최대 평점
     * 0.0 ~ 10.0 범위의 TMDB 평점
     */
    @Schema(description = "최대 평점", example = "10.0")
    @DecimalMin(value = "0.0", message = "최대 평점은 0.0 이상이어야 합니다.")
    @DecimalMax(value = "10.0", message = "최대 평점은 10.0 이하여야 합니다.")
    private Double maxRating;

    /**
     * 최소 런타임 (해당 값 포함하여 조회)
     */
    @Schema(description = "최소 런타임(분)", example = "90")
    @Min(value = 0, message = "최소 런타임은 0분 이상이어야 합니다.")
    private Integer minRuntime;

    /**
     * 최대 런타임 (해당 값 포함하여 조회)
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
     * 검색 키워드
     * 영화 제목이나 배우 이름을 검색할 때 사용
     */
    @Schema(description = "검색 키워드", example = "슈퍼맨")
    @NotBlank(message = "검색 키워드는 필수입니다.")
    @Size(min = 1, max = 100, message = "검색 키워드는 1자 이상 100자 이하여야 합니다.")
    private String keyword;

    /**
     * 검색 타입
     * title: 제목만 검색, actor: 배우만 검색, all: 제목과 배우 모두 검색
     */
    @Schema(description = "검색 타입 (title, actor, all)", example = "title")
    private String searchType = "all";

    /**
     * 복합 검색 조건 존재 여부 확인
     *
     * 제목, 장르, 배우명 중 하나라도 검색 조건이 있는지 확인
     * 주로 JOIN을 필요로 하는 복잡한 쿼리를 생성
     *
     * @return 복합 검색 조건이 있으면 true, 없으면 false
     */
    public boolean hasComplexSearch() {
        return (title != null && !title.trim().isEmpty()) ||
                genreId != null ||
                (actorName != null && !actorName.trim().isEmpty());
    }

    /**
     * 단순 필터 조건 존재 여부 확인
     *
     * 개봉연도, 평점, 런타임, 현재상영 등 Movie 테이블 내에서만 처리 가능한 조건들 확인
     * WHERE 절만으로 처리 가능한 단순한 필터링
     *
     * @return 필터 조건이 있으면 true, 없으면 false
     */
    public boolean hasFilters() {
        return releaseYear != null ||
                minRating != null ||
                maxRating != null ||
                minRuntime != null ||
                maxRuntime != null ||
                nowPlaying != null;
    }

    /**
     * 검색 조건이 하나라도 있는지 확인
     *
     * @return 검색 조건이 있으면 true, 없으면 false
     */
    public boolean hasAnySearchCriteria() {
        return hasComplexSearch() || hasFilters();
    }

    /**
     * 평점 범위가 유효한지 검증
     *
     * @return 평점 범위가 유효하면 true, 유효하지 않으면 false
     */
    public boolean isValidRatingRange() {
        if (minRating != null && maxRating != null) {
            return minRating <= maxRating;
        }
        return true;
    }

    /**
     * 런타임 범위가 유효한지 검증
     *
     * @return 런타임 범위가 유효하면 true, 유효하지 않으면 false
     */
    public boolean isValidRuntimeRange() {
        if (minRuntime != null && maxRuntime != null) {
            return minRuntime <= maxRuntime;
        }
        return true;
    }

    /**
     * 검색 타입 유효성 검증
     *
     * @return 유효한 검색 타입이면 true, 그렇지 않으면 false
     */
    public boolean isValidSearchType() {
        return searchType != null &&
                (searchType.equals("title") || searchType.equals("actor") || searchType.equals("all"));
    }
}
