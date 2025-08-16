package com.moviebuddies.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 사용자 엔티티
 * 시스템의 모든 사용자 정보를 관리하는 핵심 엔티티
 * 프로필 정보, 인증 정보, 관계 정보를 포함
 */
@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class) // JPA Auditing을 위한 리스너 등록
public class User {

    /**
     * 사용자 고유 식별자 (Primary Key)
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 사용자명 (로그인 ID)
     * 고유값, 로그인 시 사용
     */
    @Column(nullable = false, unique = true, length = 50)
    private String username;

    /**
     * 암호화된 비밀번호
     * BCrypt로 해싱되어 저장
     */
    @Column(nullable = false)
    private String password;

    /**
     * 이메일 주소
     * 고유값, 계정 복구 및 알림 용도로 사용
     */
    @Column(nullable = false, unique = true)
    private String email;

    /**
     * 사용자 닉네임
     * 다른 사용자에게 표시되는 이름
     */
    @Column(nullable = false, length = 20)
    private String nickname;

    /**
     * 프로필 이미지 파일명
     * 실제 파일은 파일 서버에 저장되고, 여기에는 파일명만 저장
     */
    @Column(name = "profile_image")
    private String profileImage;

    /**
     * 계정 활성화 상태
     * false인 경우 로그인 및 서비스 이용 불가
     */
    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    /**
     * 계정 생성 시간
     * JPA Auditing으로 자동 설정
     */
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 마지막 수정 시간
     * JPA Auditing으로 자동 업데이트
     */
    @LastModifiedDate
    @Column(name = "last_modified_at")
    private LocalDateTime lastModifiedAt;

    /**
     * 마지막 로그인 시간
     * 로그인 시 수동으로 업데이트
     */
    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    /**
     * 보낸 친구 요청들
     * 이 사용자가 다른 사용자에게 보낸 친구 요청 목록
     */
    @OneToMany(mappedBy = "requester", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Friend> sentFriendRequests = new ArrayList<>();

    /**
     * 받은 친구 요청들
     * 다른 사용자가 이 사용자에게 보낸 친구 요청 목록
     */
    @OneToMany(mappedBy = "receiver", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Friend> receivedFriendRequests = new ArrayList<>();

    /**
     * 북마크한 영화들
     * 사용자가 관심 표시한 영화 목록
     */
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Bookmark> bookmarks = new ArrayList<>();

    /**
     * 작성한 리뷰들
     * 사용자가 영화에 대해 작성한 리뷰 목록
     */
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Review> reviews = new ArrayList<>();

    /**
     * 참여한 채팅방들
     * 다대다 관계로 여러 채팅방에 참여 가능
     */
    @ManyToMany(mappedBy = "participants", fetch = FetchType.LAZY)
    @Builder.Default
    private List<ChatRoom> chatRooms = new ArrayList<>();

    /**
     * 프로필 이미지 URL 생성
     * 프로필 이미지가 있으면 해당 URL을, 없으면 기본 이미지 URL을 반환
     * @return 프로필 이미지 URL
     */
    public String getProfileImageUrl() {
        if (profileImage != null && !profileImage.isEmpty()) {
            return "/api/v1/files/profile/" + profileImage;
        }
        return "/api/v1/files/profile/default.jpg";
    }

    /**
     * 비밀번호 변경
     * 새로운 암호화된 비밀번호로 업데이트
     * 
     * @param newPassword 새로운 암호화된 비밀번호
     */
    public void changePassword(String newPassword) {
        this.password = newPassword;
    }

    /**
     * 프로필 정보 업데이트
     * null이 아닌 값들만 업데이트하며, 공백 문자열은 제거
     * 
     * @param nickname 새로운 닉네임
     * @param email 새로운 이메일
     * @param profileImage 새로운 프로필 이미지 파일명
     */
    public void updateProfile(String nickname, String email, String profileImage) {
        if (nickname != null && !nickname.trim().isEmpty()) {
            this.nickname = nickname.trim();
        }
        if (email != null && !email.trim().isEmpty()) {
            this.email = email.trim();
        }
        if (profileImage != null) {
            this.profileImage = profileImage;
        }
    }

    /**
     * 마지막 로그인 시간 업데이트
     * 로그인 성공 시 호출하여 활동 시간 기록
     */
    public void updateLastLoginAt() {
        this.lastLoginAt = LocalDateTime.now();
    }
}
