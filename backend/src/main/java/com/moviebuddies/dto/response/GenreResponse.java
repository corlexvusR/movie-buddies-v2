package com.moviebuddies.dto.response;

import com.moviebuddies.entity.Genre;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 장르 정보 응답 DTO
 * TMDB API에서 가져온 영화 장르 정보를 클라이언트에게 전달하기 위한 데이터 전송 객체
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GenreResponse {

    /**
     * 장르 ID
     */
    private Long id;

    /**
     * 장르명
     */
    private String name;

    /**
     * TMDB API의 장르 ID
     */
    private Long tmdbId;

    /**
     * Genre 엔티티를 GenreResponse DTO로 반환
     * 
     * @param genre 변환할 Genre 엔티티
     * @return GenreResponse DTO 객체
     */
    public static GenreResponse from(Genre genre) {
        return GenreResponse.builder()
                .id(genre.getId())
                .name(genre.getName())
                .tmdbId(genre.getTmdbId())
                .build();
    }
}
