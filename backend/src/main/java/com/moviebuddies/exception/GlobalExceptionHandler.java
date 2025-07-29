package com.moviebuddies.exception;

import com.moviebuddies.dto.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;


import java.util.HashMap;
import java.util.Map;

/**
 * 전역 예외 처리 핸들러
 * 애플리케이션 전체에서 발생하는 예외를 일관된 형식으로 처리하고 응답한다.
 * +
 * &#064;RestControllerAdvice를 사용하여 모든 컨트롤러에서 발생하는 예외를 가로채어 처리
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 비즈니스 로직 예외 처리
     * 사용자 정의 비즈니스 예외를 처리하여 적절한 HTTP 상태코드와 메시지를 반환
     * 
     * @param e BusinessException 비즈니스 예외
     * @return 에러 응답 객체
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Object>> handleBusinessException(BusinessException e) {
        log.error("Business exception occurred: {}", e.getMessage());
        return ResponseEntity
                .status(e.getStatus())
                .body(ApiResponse.error(e.getErrorCode(), e.getMessage()));
    }

    /**
     * 리소스 찾을 수 없음 예외 처리
     * 데이터베이스에서 요청한 엔티티를 찾을 수 없는 경우 처리
     * 
     * @param e ResourceNotFoundException 리소스 없음 예외
     * @return 404 Not Found 응답
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleResourceNotFoundException(ResourceNotFoundException e) {
        log.error("Resource not found: {}", e.getMessage());
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error("RESOURCE_NOT_FOUND", e.getMessage()));
    }

    /**
     * 인증 실패 예외 처리
     * 잘못된 사용자 인증 정보(아이디, 비밀번호)로 로그인 시도 시 처리
     *
     * @param e BadCredentialsException Spring Security 인증 실패 예외
     * @return 401 Unauthorized 응답
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiResponse<Object>> handleBadCredentialsException(BadCredentialsException e) {
        log.error("Bad credentials: {}", e.getMessage());
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error("INVALID_CREDENTIALS", "아이디 또는 비밀번호가 잘못되었습니다."));
    }

    /**
     * 접근 권한 없음 예외 처리
     * 인증은 되었지만 해당 리소스에 대해 권한이 없는 경우 처리
     *
     * @param e AccessDeniedException Spring Security 접근 거부 예외
     * @return 403 Forbidden 응답
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Object>> handleAccessDeniedException(AccessDeniedException e) {
        log.error("Access denied: {}", e.getMessage());
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error("ACCESS_DENIED", "접근 권한이 없습니다."));
    }

    /**
     * 입력값 검증 실패 예외 처리
     * @Valid 애노테이션으로 검증된 요청 객체의 유효성 검사 실패 시 처리
     * 각 필드별 검증 오류 메시지를 상세히 반환
     *
     * @param e MethodArgumentNotValidException 또는 BindException
     * @return 400 Bad Request 응답과 필드별 오류 메시지
     */
    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
    public ResponseEntity<ApiResponse<Object>> handleValidationException(BindException e) {
        log.error("Validation failed: {}", e.getMessage());
    
        // 필드별 검증 오류 메시지 수정
        Map<String, String> validationErrors = new HashMap<>();
        e.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            validationErrors.put(fieldName, errorMessage);
        });

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.<Object>builder()
                        .success(false)
                        .errorCode("VALIDATION_FAILED")
                        .message("입력값 검증에 실패했습니다.")
                        // 필드별 오류 상세 정보 포함
                        .data(validationErrors) 
                        .build());
    }

    /**
     * 파일 업로드 크기 초과 예외 처리
     * 설정된 최대 파일 업로드 크기를 초과한 경우 처리
     * 
     * @param e MaxUploadSizeExceededException 파일 크기 초과 예외
     * @return 400 Bad Request 응답
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiResponse<Object>> handleMaxUploadSizeExceededException(MaxUploadSizeExceededException e) {
        log.error("File upload size exceeded: {}", e.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("FILE_SIZE_EXCEEDED", e.getMessage()));
    }

    /**
     * 잘못된 인수 예외 처리
     * 메서드 호출 시 잘못된 매개변수가 전달된 경우 처리
     *
     * @param e IllegalArgumentException 잘못된 인수 예외
     * @return 400 Bad Request 응답
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Object>> handleIllegalArgumentException(IllegalArgumentException e) {
        log.error("Illegal argument: {}", e.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("INVALID_ARGUMENT", e.getMessage()));
    }

    /**
     * 예상하지 못한 일반 예외 처리
     * 위에서 처리되지 않은 모든 예외에 대한 fallback 처리
     * 보안을 위해 상세한 에러 정보는 로그에만 기록하고 일반적인 메시지만 반환
     * 
     * @param e Exception 모든 예외
     * @return 500 Internal Server Error 응답
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleGenericException(Exception e) {
        // 스택 트레이스 포함 로깅
        log.error("Unexpected error occurred: ", e);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("INTERNAL_SERVER_ERROR", "서버 내부 오류가 발생했습니다."));
    }
}
