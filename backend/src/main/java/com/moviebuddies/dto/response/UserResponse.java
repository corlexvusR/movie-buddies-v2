package com.moviebuddies.dto.response;

import com.moviebuddies.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 사용자 정보 응답 DTO
 *
 * 클라이언트에게 사용자 정보를 전송할 때 사용하는 객체
 * 민감한 정보(비밀번호 등)는 제외하고 필요한 정보만 포함
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {

    /**
     * 사용자 고유 식별자
     */
    private Long id;

    /**
     * 사용자명 (로그인 ID)
     */
    private String username;

    /**
     * 사용자 이메일 주소
     */
    private String email;

    /**
     * 사용자 닉네임 (표시명)
     */
    private String nickname;

    /**
     * 프로필 이미지 URL
     * User 엔티티의 getProfileImageUrl() 메서드를 통해 생성된 완전한 URL
     */
    private String profileImageUrl;

    /**
     * 계정 생성 일시
     */
    private LocalDateTime createdAt;

    /**
     * 마지막 로그인 일시
     */
    private LocalDateTime lastLoginAt;

    /**
     * User 엔티티로부터 UserResponse DTO를 생성하는 정적 팩토리 메서드
     *
     * Entity를 DTO로 변환할 때 사용하며, 민감한 정보는 제외하고 클라이언트에게 전송해도 안전한 정보만 포함
     *
     * @param user 변환할 User 엔티티
     * @return 변환할 UserResponse DTO
     */
    public static UserResponse from(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .profileImageUrl(user.getProfileImageUrl()) // 완전한 URL 생성
                .createdAt(user.getCreatedAt())
                .lastLoginAt(user.getLastLoginAt())
                .build();
    }
}
