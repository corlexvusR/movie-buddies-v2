package com.moviebuddies.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 채팅방 엔티티
 * 영화 정보 사이트의 채팅 기능을 제공
 * 모든 채팅방은 공개되어 있으며 누구나 참여 가능
 */
@Entity
@Table(name = "chat_rooms")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class ChatRoom {

    /**
     * 채팅방 고유 식별자 (Primary Key)
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 채팅방 이름
     * 고유값으로 중복될 수 없으며 채팅방을 식별하는 주요 정보
     */
    @Column(nullable = false, unique = true, length = 100)
    private String name;

    /**
     * 채팅방 설명
     * 채팅방의 목적이나 주제에 대한 간단한 설명
     */
    @Column(length = 255)
    private String description;

    /**
     * 최대 참가자 수
     * 채팅방에 동시에 참여할 수 있는 최대 인원수 (기본값: 50명)
     */
    @Column(name = "max_participants")
    @Builder.Default
    private Integer maxParticipants = 50;

    /**
     * 채팅방 활성화 상태
     * false인 경우 새로운 참가자 입장이나 메시지 발송이 불가능
     */
    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    /**
     * 채팅방 생성자
     * 채팅방을 만든 사용자 정보
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

    /**
     * 채팅방 생성 시간
     * JPA Auditing으로 자동 설정
     */
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 채팅방 참가자 목록
     * 다대다 관계로 여러 사용자가 여러 채팅방에 참여 가능
     */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "chat_room_participants",
            joinColumns = @JoinColumn(name = "chat_room_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    @Builder.Default
    private List<User> participants = new ArrayList<>();

    /**
     * 채팅방 참가
     * 채팅방 참가 조건을 만족하는 경우 참가자 목록에 추가
     *
     * @param user 참가를 원하는 사용자
     * @return 참가가 성공한 경우 true, 실패한 경우 false
     */
    public boolean joinRoom(User user) {
        if (canJoin(user)) {
            participants.add(user);
            return true;
        }
        return false;
    }

    /**
     * 채팅방 나가기
     * 현재 참가자인 경우에만 참가자 목록에서 제거
     *
     * @param user 퇴장을 원하는 사용자
     * @return 퇴장이 성공한 경우 true, 실패한 경우 false
     */
    public boolean leaveRoom(User user) {
        if (isParticipant(user)) {
            participants.remove(user);
            return true;
        }
        return false;
    }

    /**
     * 익명 표시용 사용자 식별자 생성
     * 채팅방 내에서만 유효한 임시 식별자로 사용자의 실제 정보를 숨김
     * 사용자 ID와 채팅방 ID를 조합한 해시값을 기반으로 일관된 익명 이름 생성
     *
     * @param user 익명 식별자를 생성할 사용자
     * @return 채팅방 참가자인 경우 "익명XXX" 형태의 문자열, 비참가자인 경우 "Unknown"
     */
    public String getAnonymousDisplayName(User user) {
        if (!isParticipant(user)) {
            return "Unknown";
        }

        // 사용자 ID와 채팅방 ID를 조합한 해시 기반 익명 이름
        int hash = Math.abs((user.getId().toString() + this.id.toString()).hashCode());
        // 1-999 범위
        int displayNumber = (hash % 999) + 1;

        return "익명" + String.format("%03d", displayNumber);
    }

    /**
     * 채팅방 메시지 목록
     * 채팅방에 속한 모든 메시지들
     */
    @OneToMany(mappedBy = "chatRoom", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<ChatMessage> messages = new ArrayList<>();

    /**
     * 특정 사용자가 채팅방에 참가하고 있는지 확인
     *
     * @param user 확인할 사용자
     * @return 참가하고 있으면 true
     */
    public boolean isParticipant(User user) {
        return participants.contains(user);
    }

    /**
     * 현재 참가자 수 조회
     *
     * @return 현재 채팅방에 참가하고 있는 사용자 수
     */
    public int getParticipantCount() {
        return participants.size();
    }

    /**
     * 채팅방이 가득 찼는지 확인
     *
     * @return 최대 참가자 수에 도달한 경우 true
     */
    public boolean isFull() {
        return participants.size() >= maxParticipants;
    }

    /**
     * 사용자가 채팅방에 참가할 수 있는지 확인
     * 채팅방이 활성화되어 있고, 가득 차지 않았으며, 아직 참가하지 않은 경우에만 가능
     *
     * @param user 참가하려는 사용자
     * @return 참가 가능한 경우 true
     */
    public boolean canJoin(User user) {
        return isActive && !isFull() && !isParticipant(user);
    }

    /**
     * 채팅방 생성자인지 확인
     * 정보 제공 목적으로만 사용되며, 실제 권한 부여와는 무관
     *
     * @param user 확인할 사용자
     * @return 해당 사용자가 채팅방 생성자인 경우 true
     */
    public boolean isCreatedBy(User user) {
        return createdBy != null && createdBy.equals(user);
    }
}
