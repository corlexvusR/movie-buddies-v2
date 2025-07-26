package com.moviebuddies.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 표준화된 API 응답 래퍼 클래스
 * + 
 * 모든 API 응답을 일관된 형태로 제공하기 위한 제네릭 클래스
 *
 * 주요 특징:
 * - 제네릭 타입으로 다양한 데이터 타입 지원
 * - Builder 패턴으로 유연한 객체 생성
 * - Static factory 메서드로 편리한 응답 생성
 * - null 필드 자동 제외로 깔끔한 Json 응답
 *
 * @param <T> 응답 데이터의 타입 (User, Movie, List<Movie> 등)
 */
@Data   // Getter, Setter, toString, equals, hashCode 자동 생성
@Builder    // Builder 패턴 지원 (유연한 객체 생성)
@NoArgsConstructor  // 기본 생성자 (JSON 역직렬화용)
@AllArgsConstructor // 모든 필드를 받는 생성자
@JsonInclude(JsonInclude.Include.NON_NULL)  // null 값인 필드는 JSON에서 제외
public class ApiResponse<T> {

    /**
     * API 호출 성공 여부
     * true: 성공, false: 실패
     */
    private boolean success;

    /**
     * 응답 메시지
     * 성공시와 실패시 메시지 구분
     */
    private String message;

    /**
     * 실제 응답 데이터 (제네릭 타입)
     * 에러 응답에서는 null이 되어 JSON에서 제외됨
     */
    private T data;

    /**
     * 에러 코드 (실패시에만 사용)
     * 클라이언트에서 에러 타입 별로 다른 처리를 할 때 사용
     */
    private String errorCode;

    /**
     * 응답 생성 시간
     * ISO 8601 형식으로 자동 직렬화됨
     */
    private LocalDateTime timestamp;

    /**
     * 데이터만 포함한 성공 응답 생성
     *
     * @param data 응답 데이터
     * @param <T> 데이터 타입
     * @return 성공 응답 객체
     */
    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * 메시지와 데이터를 포함한 성공 응답 생성
     *
     * @param message 성공 메시지
     * @param data 응답 데이터
     * @param <T> 데이터 타입 
     * @return 성공 응답 객체
     */
    public static <T> ApiResponse<T> success(String message, T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * 메시지만 포함한 성공 응답 생성 (데이터 없음)
     * 
     * @param message 성공 메시지
     * @param <T> 데이터 타입 (실제로는 사용되지 않음)
     * @return 성공 응답 객체
     */
    public static <T> ApiResponse<T> success(String message) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * 에러 코드와 메시지를 포함한 에러 응답 생성
     *
     * @param errorCode 에러 분류 코드 (클라이언트에서 에러 타입 구분용)
     * @param message 사용자에게 표시할 에러 메시지
     * @param <T> 데이터 타입 (에러 응답에서는 사용되지 않음)
     * @return 에러 응답 객체 
     */
    public static <T> ApiResponse<T> error(String errorCode, String message) {
        return ApiResponse.<T>builder()
                .success(false)
                .errorCode(errorCode)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * 메시지만 포함한 에러 응답 생성 (에러 코드 없음)
     * 
     * @param message 에러 메시지
     * @param <T> 데이터 타입 (에러 응답에는 사용되지 않음)
     * @return 에러 응답 객체
     */
    public static <T> ApiResponse<T> error(String message) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
    }
}
