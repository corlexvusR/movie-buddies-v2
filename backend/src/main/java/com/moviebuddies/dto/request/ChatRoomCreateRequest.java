package com.moviebuddies.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 채팅방 생성 요청 DTO
 * 공개 채팅방만 지원하는 구조
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatRoomCreateRequest {

    /**
     * 채팅방 이름
     * 고유하고 다른 사용자들이 검색할 수 있는 식별자 역할
     */
    @NotBlank(message = "채팅방 이름은 필수입니다.")
    @Size(min = 2, max = 50, message = "채팅방 이름은 2-50자 사이여야 합니다.")
    private String name;

    /**
     * 채팅방 설명 (선택사항)
     * 채팅방의 목적이나 주제에 대해 설명 가능
     */
    @Size(max = 255, message = "설명은 최대 255자까지 입력 가능합니다.")
    private String description;

    /**
     * 최대 참가자 수 
     * 적절한 대화 환경 유지
     * 기본값 50명, 최소 2명에서 최대 100명까지 설정 가능
     */
    @Min(value = 2, message = "최소 참가자 수는 2명입니다.")
    @Max(value = 100, message = "최대 참가자 수는 100명입니다.")
    @Builder.Default
    private Integer maxParticipants = 50;
}
