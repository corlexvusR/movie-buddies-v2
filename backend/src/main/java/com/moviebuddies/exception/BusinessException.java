package com.moviebuddies.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * 비즈니스 로직 예외 클래스
 * 애플리케이션의 비즈니스 규칙 위반이나 예상 가능한 오류 상황을 나타내는 사용자 정의 예외
 * +
 * 에러 코드, 메시지, HTTP 상태 코드를 포함하여 일관된 예외 처리 제공
 */
@Getter
public class BusinessException extends RuntimeException {

    // 에러 식별을 위한 코드 ("USER_NOT_FOUND" 등)
    private final String errorCode;
    // HTTP 응답 상태 코드
    private final HttpStatus status;

    /**
     * 완전한 생성자
     *
     * @param errorCode 에러 코드
     * @param message 에러 메시지
     * @param status HTTP 상태 코드
     */
    public BusinessException(String errorCode, String message, HttpStatus status) {
        super(message);
        this.errorCode = errorCode;
        this.status = status;
    }

    /**
     * 기본 상태 코드(400 Bad Request)를 사용하는 생성자
     * 
     * @param errorCode 에러 코드
     * @param message 에러 메시지
     */
    public BusinessException(String errorCode, String message) {
        this(errorCode, message, HttpStatus.BAD_REQUEST);
    }

    // 자주 사용되는 예외들을 위한 정적 팩토리 메서드

    /**
     * 400 Bad Request 예외 생성
     * 잘못된 요청이나 입력값 오류 시 사용
     */
    public static BusinessException badRequest(String message) {
        return new BusinessException("BAD_REQUEST", message, HttpStatus.BAD_REQUEST);
    }

    /**
     * 401 Unauthorized 예외 생성
     * 인증이 필요하거나 인증 실패 시 사용
     */
    public static BusinessException unauthorized(String message) {
        return new BusinessException("UNAUTHORIZED", message, HttpStatus.UNAUTHORIZED);
    }

    /**
     * 403 Forbidden 예외 생성
     * 권한이 부족한 경우 사용
     */
    public static BusinessException forbidden(String message) {
        return new BusinessException("FORBIDDEN", message, HttpStatus.FORBIDDEN);
    }

    /**
     * 404 Not Found 예외 생성
     * 요청한 리소스가 존재하지 않는 경우 사용
     */
    public static BusinessException notFound(String message) {
        return new BusinessException("NOT_FOUND", message, HttpStatus.NOT_FOUND);
    }

    /**
     * 409 Conflict 예외 생성
     * 리소스 충돌이나 중복 등의 문제 시 사용 (이미 존재하는 사용자 등)
     */
    public static BusinessException conflict(String message) {
        return new BusinessException("CONFLICT", message, HttpStatus.CONFLICT);
    }

    /**
     * 500 Internal Server Error 예외 생성
     * 예상하지 못한 서버 내부 오류 시 사용
     */
    public static BusinessException internalServerError(String message) {
        return new BusinessException("INTERNAL_SERVER_ERROR", message, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
