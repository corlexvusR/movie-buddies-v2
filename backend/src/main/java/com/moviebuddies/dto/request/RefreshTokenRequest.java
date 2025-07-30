package com.moviebuddies.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 토큰 새로고침 요청 DTO
 *
 * 만료된 액세스 토큰을 새로 발급받기 위해 리프레시 토큰을 전송할 때 사용하는 객체
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RefreshTokenRequest {

    /**
     * 새로운 액세스 토큰 발급을 위한 리프레시 토큰
     * 로그인 시 발급받은 유효한 리프레시 토큰이어야 함
     */
    @NotBlank(message = "리프레시 토큰은 필수입니다.")
    private String refreshToken;
}
