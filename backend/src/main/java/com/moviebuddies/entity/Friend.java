package com.moviebuddies.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 친구 관계 엔티티
 * 사용자 간의 친구 관계와 친구 요청을 관리
 * 요청자(requester)와 수신자(receiver) 간의 관계를 나타냄
 */
@Entity
@Table(name = "friends", uniqueConstraints = @UniqueConstraint(columnNames = {"requester_id", "receiver_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class Friend {

    /**
     * 친구 관계 고유 식별자
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 친구 요청을 보낸 사용자
     * 지연 로딩으로 설정하여 성능 최적화
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requester_id", nullable = false)
    private User requester;

    /**
     * 친구 요청을 받은 사용자
     * 지연 로딩으로 설정하여 성능 최적화
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_id", nullable = false)
    private User receiver;

    /**
     * 친구 관계 상태
     * PENDING: 요청 대기, ACCEPTED: 수락됨, DECLINED: 거절됨
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private FriendStatus status = FriendStatus.PENDING;

    /**
     * 친구 요청 생성 시간
     * JPA Auditing으로 자동 설정
     */
    @CreatedDate
    @Column(name = "create_at", nullable = false, updatable = false)
    private LocalDateTime createAt;

    /**
     * 친구 요청 수락 시간
     * 수락된 경우에만 값이 설정됨
     */
    @Column(name = "accepted_at")
    private LocalDateTime acceptedAt;

    /**
     * 친구 관계 상태 열거형
     * 각 상태에 대한 한글 설명 포함됨
     */
    public enum FriendStatus {
        PENDING("대기중"),
        ACCEPTED("수락됨"),
        DECLINED("거절됨");

        private final String description;

        FriendStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 친구 요청 수락
     * 상태를 ACCEPTED로 변경하고 수락 시간을 기록
     */
    public void accept() {
        this.status = FriendStatus.ACCEPTED;
        this.acceptedAt = LocalDateTime.now();
    }

    /**
     * 친구 요청 거절
     * 상태를 DECLINED으로 변경
     */
    public void decline() {
        this.status = FriendStatus.DECLINED;
    }

    /**
     * 친구 관계가 수락된 상태인지 확인
     *
     * @return 수락된 상태이면 true
     */
    public boolean isAccepted() {
        return this.status == FriendStatus.ACCEPTED;
    }

    /**
     * 친구 요청이 대기 중인지 확인
     *
     * @return 대기 중이면 true
     */
    public boolean isPending() {
        return this.status == FriendStatus.PENDING;
    }

    /**
     * 현재 사용자의 상대방 조회
     * 양방향 관계에서 현재 사용자가 아닌 상대방을 반환
     * 
     * @param currentUser 현재 사용자
     * @return 상대방 사용자
     * @throws IllegalArgumentException 현재 사용자가 이 관계에 포함되지 않은 경우
     */
    public User getOtherUser(User currentUser) {
        if (requester.equals(currentUser)) {
            return receiver;
        } else if (receiver.equals(currentUser)) {
            return requester;
        }
        throw new IllegalArgumentException("현재 사용자가 이 친구 관계에 포함되어 있지 않습니다.");
    }

    /**
     * 주어진 사용자가 친구 요청을 보낸 사용자인지 확인
     * 
     * @param user 확인할 사용자
     * @return 요청자이면 true
     */
    public boolean isRequester(User user) {
        return requester.equals(user);
    }

    /**
     * 주어진 사용자가 친구 요청을 받은 사용자인지 확인
     * 
     * @param user 확인할 사용자
     * @return 수신자이면 true
     */
    public boolean isReceiver(User user) {
        return receiver.equals(user);
    }
}
