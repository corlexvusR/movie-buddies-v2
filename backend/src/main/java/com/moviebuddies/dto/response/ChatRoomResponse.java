package com.moviebuddies.dto.response;

import com.moviebuddies.entity.ChatRoom;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 채팅방 응답 DTO
 * 채팅방 정보를 클라이언트에 전달하며 참가자 개인정보는 익명화
 * 채팅방 목록, 상세 정보 등에서 공통으로 사용
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatRoomResponse {

    /**
     * 채팅방 고유 식별자
     */
    private Long id;

    /**
     * 채팅방 이름
     * 사용자들이 채팅방을 식별하는 주요 정보
     */
    private String name;

    /**
     * 채팅방 설명
     * 채팅방의 목적이나 주제에 대한 설명 (선택사항)
     */
    private String description;

    /**
     * 최대 참가자 수
     * 채팅방 생성 시 설정된 인원 제한
     */
    private Integer maxParticipants;

    /**
     * 현재 참가자 수
     * 실시간으로 업데이트되는 현재 참가 인원
     */
    private Integer currentParticipants;

    /**
     * 채팅방 활성화 상태
     * false인 경우 새로운 참가나 메시지 전송 불가
     */
    private Boolean isActive;

    /**
     * 채팅방 생성자 익명 표시명
     * 실제 생성자 정보 대신 익명화된 표시명
     * 생성자도 특별한 권한은 없으므로 단순 정보 제공용
     */
    private String createdByDisplayName;

    /**
     * 채팅방 생성 시간
     */
    private LocalDateTime createdAt;

    /**
     * ChatRoom 엔티티로부터 응답 DTO 생성
     * 모든 개인 정보는 익명화
     *
     * @param chatRoom 변환할 ChatRoom 엔티티
     * @return 익명화된 채팅 응답 DTO
     */
    public static ChatRoomResponse from(ChatRoom chatRoom) {
        return ChatRoomResponse.builder()
                .id(chatRoom.getId())
                .name(chatRoom.getName())
                .description(chatRoom.getDescription())
                .maxParticipants(chatRoom.getMaxParticipants())
                .currentParticipants(chatRoom.getParticipantCount())
                .isActive(chatRoom.getIsActive())
                .createdByDisplayName(chatRoom.getAnonymousDisplayName(chatRoom.getCreatedBy()))
                .createdAt(chatRoom.getCreatedAt())
                .build();
    }
}
