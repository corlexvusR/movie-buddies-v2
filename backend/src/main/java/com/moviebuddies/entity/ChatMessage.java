package com.moviebuddies.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 채팅 메시지 엔티티
 * 채팅방 내에서 사용자들이 주고 받는 텍스트 메시지를 관리
 * 영화 정보 사이트의 커뮤니티 기능을 위한 단순한 텍스트 메시지만 지원
 */
@Entity
@Table(name = "chat_messages", indexes = {@Index(name = "idx_chat_room_created", columnList = "chat_room_id, created_at")})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class ChatMessage {

    /**
     * 메시지 고유 식별자(Primary Key)
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 메시지가 속한 채팅방
     * 채팅방이 삭제되면 관련 메시지들도 함께 삭제됨
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_room_id", nullable = false)
    private ChatRoom chatRoom;

    /**
     * 메시지 발송자
     * 사용자 정보와 연결되어 메시지 작성자를 식별
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    /**
     * 메시지 내용
     * 텍스트 형태의 메시지
     */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    /**
     * 메시지 생성 시간
     * JPA Auditing으로 자동 설정되며 수정 불가
     */
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 메시지가 특정 사용자로부터 온 것인지 확인
     *
     * @param user 확인할 사용자
     * @return 해당 사용자가 발송한 메시지인 경우 true
     */
    public boolean isFromUser(User user) {
        return sender.equals(user);
    }
}