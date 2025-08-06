package com.moviebuddies.dto.response;

import com.moviebuddies.entity.Bookmark;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 북마크 정보 응답 DTO
 * 사용자의 북마크 목록 조회 시 북마크 상세 정보를 클라이언트에게 전달
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookmarkResponse {

    /**
     * 북마크 고유 ID
     */
    private Long id;

    /**
     * 북마크된 영화 정보
     */
    private MovieListResponse movie;

    /**
     * 북마크 생성 시간
     */
    private LocalDateTime createdAt;

    /**
     * Bookmark 엔티티를 BookmarkResponse DTO로 변환
     * 연관된 영화 정보도 함께 변환하여 포함
     *
     * @param bookmark 반환할 Bookmark 엔티티
     * @return BookmarkResponse DTO 객체
     */
    public static BookmarkResponse from(Bookmark bookmark) {
        return BookmarkResponse.builder()
                .id(bookmark.getId())
                .movie(MovieListResponse.from(bookmark.getMovie()))
                .createdAt(bookmark.getCreatedAt())
                .build();
    }
}
