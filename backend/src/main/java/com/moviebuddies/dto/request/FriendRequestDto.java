package com.moviebuddies.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 친구 요청 DTO
 * 
 * 친구 요청을 보낼 때 사용되는 데이터 전송 객체
 * 친구로 추가하고자 하는 사용자의 사용자명을 포함
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FriendRequestDto {

    /**
     * 친구 요청을 받을 사용자의 사용자명
     *
     * 사용자 검색 결과에서 선택한 사용자에게 친구 요청을 보낼 때 사용
     * 공백이나 null 값은 허용되지 않음
     */
    @NotBlank(message = "사용자명은 필수입니다.")
    private String username;
}
