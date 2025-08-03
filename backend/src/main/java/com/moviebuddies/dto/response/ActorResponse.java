package com.moviebuddies.dto.response;

import com.moviebuddies.entity.Actor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 배우 정보 응답 DTO
 * TMDB API에서 가져온 배우 정보를 클라이언트에게 전달하기 위한 데이터 전송 객체
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActorResponse {

    /**
     * 배우 ID
     */
    private Long id;

    /**
     * 배우 이름
     */
    private String name;

    /**
     * 프로필 이미지 URL (TMDB 이미지 서버 기반)
     */
    private String profileImageUrl;

    /**
     * TMDB 인기도 점수
     */
    private Double popularity;

    /**
     * TMDB API의 배우 ID
     */
    private Long tmdbId;

    /**
     * Actor 엔티티를 ActorResponse DTO로 변환
     *
     * @param actor 변환할 Actor 엔티티
     * @return ActorResponse DTO 객체
     */
    public static ActorResponse from(Actor actor) {
        return ActorResponse.builder()
                .id(actor.getId())
                .name(actor.getName())
                .profileImageUrl(actor.getProfileImageUrl())
                .popularity(actor.getPopularity())
                .tmdbId(actor.getTmdbId())
                .build();
    }
}
