package com.moviebuddies.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 회원가입 요청 DTO
 *
 * 새로운 사용자 회원가입 시 클라이언트에서 전송하는 요청 데이터를 담는 객체
 * 각 필드에 대한 상세한 유효성 검증 규칙을 포함
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SignupRequest {

    /**
     * 회원가입할 사용자명
     * 3-20자 사이의 문자열이어야 하며, 로그인 시 사용
     */
    @NotBlank(message = "사용자명은 필수입니다.")
    @Size(min = 3, max = 20, message = "사용자명은 3-20자 사이여야 합니다.")
    private String username;

    /**
     * 회원가입할 사용자의 비밀번호
     * 보안을 위해 최소 8자 이상이어야 함
     */
    @NotBlank(message = "비밀번호는 필수입니다.")
    @Size(min = 8, max = 100, message = "비밀번호는 8자 이상이어야 합니다.")
    private String password;

    /**
     * 회원가입할 사용자의 이메일 주소
     * 유효한 이메일 형식이어야 하며, 계정 복구 등에 사용
     */
    @NotBlank(message = "이메일은 필수입니다.")
    @Email(message = "유효한 이메일 형식이어야 합니다.")
    private String email;

    /**
     * 회원가입할 사용자의 닉네임
     * 2-20자 사이의 문자열이며, 다른 사용자에게 표시되는 이름
     */
    @NotBlank(message = "닉네임은 필수입니다.")
    @Size(min = 2, max = 20, message = "닉네임은 2-20자 사이여야 합니다.")
    private String nickname;
}
