package com.moviebuddies.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 사용자 정보 수정 요청 DTO
 * 
 * 사용자 프로필 수정 시 클라이언트에서 전송하는 요청 데이터를 담는 객체
 * 모든 필드는 선택적(Optional)이며, null이 아닌 값들만 업데이트
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdateRequest {
    /**
     * 수정할 닉네임
     * 2-20자 사이의 문자열이며, null인 경우 수정하지 않음
     */
    @Size(min = 2, max = 20, message = "닉네임은 2-20자 사이여야 합니다.")
    private String nickname;

    /**
     * 수정할 이메일 주소
     * 유효한 이메일 형식이어야 하며, null인 경우 수정하지 않음
     */
    @Email(message = "유효한 이메일 형식이어야 합니다.")
    private String email;
}
