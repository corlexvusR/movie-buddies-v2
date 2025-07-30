package com.moviebuddies.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * JWT 토큰 응답 DTO
 *
 * 로그인 성공 또는 토큰 새로고침 시 클라이언트에게 전송하는 응답 데이터를 담는 객체
 * 액세스 토큰, 리프레시 토큰, 사용자 정보를 포함
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JwtResponse {

    /**
     * JWT 액세스 토큰
     * API 호출 시 Authorization 헤더에 포함하여 사용
     * 상대적으로 짧은 만료 시간을 가짐
     */
    private String accessToken;

    /**
     * JWT 리프레시 토큰
     * 액세스 토큰이 만료되었을 때 새로운 토큰을 발급받기 위해 사용
     * 액세스 토큰보다 긴 만료 시간을 가짐
     */
    private String refreshToken;

    /**
     * 토큰 타입
     * HTTP Authorization 헤더에서 사용할 토큰 타입
     * 일반적으로 "Beared"가 사용됨
     */
    private String tokenType;

    /**
     * 인증된 사용자 정보
     * 로그인한 사용자의 기본 정보를 포함
     */
    private UserResponse user;
}
