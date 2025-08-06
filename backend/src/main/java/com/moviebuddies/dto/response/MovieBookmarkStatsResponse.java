package com.moviebuddies.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 영화별 북마크 통계 응답 DTO
 * 인기 영화 랭킹이나 북마크 기반 추천 시스템에서 활용
 * Repository의 findMostBookmarkedMovies 쿼리 결과를 매핑할 때 사용
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MovieBookmarkStatsResponse {

    private Long movieId;
    private String movieTitle;
    private Long bookmarkCount;
}
