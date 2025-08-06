package com.moviebuddies.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 사용자 북마크 통계 응답 DTO
 * 사용자 프로필에서 북마크 통계 정보를 표시할 때 사용
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookmarkStatsResponse {

    /**
     * 사용자의 총 북마크 개수
     */
    private long totalBookmarks;
}
