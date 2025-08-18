package com.moviebuddies.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 채팅 메시지 전송 요청 DTO
 * 텍스트 메시지만 지원
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageRequest {

    /**
     * 메시지 내용
     * 빈 문자열이나 공백만으로는 전송 불가
     * 500자 제한
     */
    @NotBlank(message = "메시지 내용은 필수입니다.")
    @Size(max = 500, message = "메시지는 최대 500자까지 입력 가능합니다.")
    private String content;
}
