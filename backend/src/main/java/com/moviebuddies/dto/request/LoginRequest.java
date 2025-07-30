package com.moviebuddies.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 로그인 요청 DTO
 *
 * 사용자 로그인 시 클라이언트에서 전송하는 요청 데이터를 담는 객체
 * Bean Validation을 통해 입력값 검증을 수행
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {

    /**
     * 로그인할 사용자명
     * 공백이나 null 값은 허용하지 않음
     */
    @NotBlank(message = "사용자명은 필수입니다.")
    private String username;

    /**
     * 로그인할 사용자의 비밀번호
     * 공백이나 null 값은 허용하지 않음
     */
    @NotBlank(message = "비밀번호는 필수입니다.")
    private String password;
}
