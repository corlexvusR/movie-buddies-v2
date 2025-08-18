package com.moviebuddies.dto.response;

import com.moviebuddies.entity.ChatMessage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 채팅 메시지 응답 DTO
 * 익명성을 보장하는 메시지 정보
 * 실시간 WebSocket과 REST API 양쪽에서 모두 사용
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageResponse {

    /**
     * 메시지 고유 식별자
     */
    private Long id;

    /**
     * 메시지가 속한 채팅방 ID
     */
    private Long chatRoomId;

    /**
     * 발송자 익명 표시명
     * "익명001" 등의 형태로 표시
     * 같은 채팅방에서는 일관적인 익명 이름 유지
     */
    private String senderDisplayName;

    /**
     * 메시지 내용
     */
    private String content;

    /**
     * 메시지 생성 시간
     */
    private LocalDateTime createdAt;

    /**
     * 시스템 메시지 여부
     * true인 경우 입장/퇴장 알림 등의 시스템 생성 메시지
     */
    private Boolean isSystem;

    /**
     * ChatMessage 엔티티로부터 익명 응답 생성
     * 일반 사용자 메시지를 익명화하여 응답 DTO로 변환
     * 
     * @param message 변환할 ChatMessage 엔티티
     * @return 익명화된 메시지 응답 DTO
     */
    public static ChatMessageResponse from(ChatMessage message) {
        return ChatMessageResponse.builder()
                .id(message.getId())
                .chatRoomId(message.getChatRoom().getId())
                .senderDisplayName(message.getAnonymousDisplayName())
                .content(message.getContent())
                .createdAt(message.getCreatedAt())
                .isSystem(false)
                .build();
    }

    /**
     * 시스템 메시지 생성용 정적 메서드
     * 채팅방 생성 알림, 입장/퇴장 알림 등에 사용
     * 데이터베이스에 저장되지 않는 임시 메시지
     *
     * @param chatRoomId 시스템 메시지를 전송할 채팅방 ID
     * @param content 시스템 메시지 내용
     * @return 시스템 메시지 응답 DTO
     */
    public static ChatMessageResponse createSystemMessage(Long chatRoomId, String content) {
        return ChatMessageResponse.builder()
                .chatRoomId(chatRoomId)
                .senderDisplayName("시스템")
                .content(content)
                .createdAt(LocalDateTime.now())
                .isSystem(true)
                .build();
    }
}
