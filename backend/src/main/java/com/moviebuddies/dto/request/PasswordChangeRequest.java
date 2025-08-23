package com.moviebuddies.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 비밀번호 변경 요청 DTO
 * 
 * 사용자가 비밀번호를 변경할 때 필요한 현재 비밀번호와 새로운 비밀번호를 담는 객체
 * 현재 비밀번호 확인 후 새 비밀번호로 변경하는 방식 사용
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PasswordChangeRequest {

    /**
     * 현재 비밀번호
     * 비밀번호 변경 시 현재 비밀번호를 먼저 확인
     */
    @NotBlank(message = "현재 비밀번호는 필수입니다.")
    private String currentPassword;

    /**
     * 새로운 비밀번호
     */
    @NotBlank(message = "새 비밀번호는 필수입니다.")
    @Size(min = 8, max = 100, message = "비밀번호는 8자 이상이어야 합니다.")
    private String newPassword;

    /**
     * 새 비밀번호 확인
     * 사용자 실수를 방지하기 위한 비밀번호 재확인 필드
     */
    @NotBlank(message = "새 비밀번호 확인은 필수입니다.")
    private String confirmPassword;
}
