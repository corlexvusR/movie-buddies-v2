package com.moviebuddies.exception;

/**
 * 리소스 찾을 수 없음 예외 클래스
 * 데이터베이스나 외부 시스템에서 요청한 리소스(엔티티)를 찾을 수 없는 경우 발생
 * +
 * 다양한 생성자를 제공하여 상황에 맞는 명확한 에러 메시지 생성 지원
 */
public class ResourceNotFoundException extends RuntimeException {

    /**
     * 기본 생성자
     * 직접 메시지를 지정하는 경우 사용
     * 
     * @param message 에러 메시지
     */
    public ResourceNotFoundException(String message) {
        super(message);
    }

    /**
     * 리소스명, 필드명, 필드값을 이용한 생성자
     * 특정 필드로 검색했으나 찾을 수 없는 경우 사용
     * 예: "사용자를 찾을 수 없습니다. 이메일: user@example.com"
     * 
     * @param resourceName 리소스명 (사용자, 영화 등)
     * @param fieldName 검색 필드명 (이메일, 제목 등)
     * @param fieldValue 검색 필드값
     */
    public ResourceNotFoundException(String resourceName, String fieldName, Object fieldValue) {
        super(String.format("%s를 찾을 수 없습니다. %s: %s", resourceName, fieldName, fieldValue));
    }

    /**
     * 리소스 명과 ID를 이용한 생성자
     * ID로 검색했으나 찾을 수 없는 경우 사용 (가장 일반적인 케이스)
     * 예: "사용자를 찾을 수 없습니다. ID: 123"
     *
     * @param resourceName 리소스명 (사용자, 영화 등)
     * @param id 검색한 ID
     */
    public ResourceNotFoundException(String resourceName, Long id) {
        super(String.format("%s를 찾을 수 없습니다. ID: %d", resourceName, id));
    }
}
