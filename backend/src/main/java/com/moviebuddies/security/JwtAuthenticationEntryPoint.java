package com.moviebuddies.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.moviebuddies.dto.response.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * JWT 인증 실패 처리 클래스
 * +
 * 주요 기능:
 * - 인증되지 않은 요청에 대한 응답 처리
 * - 401 Unauthorized 상태 코드 반환
 * - 표준화된 JSON 에러 응답 생성
 * - 클라이언트에게 인증 실패 이유 제공
 * +
 * 실행 시나리오:
 * 1. JWT 토큰이 없는 요청
 * 2. 유효하지 않는 JWT 토큰
 * 3. 만료된 JWT 토큰
 * 4. 권한이 없는 리소스 접근 시도
 * +
 * Spring Security Filter Chain에서 호출 순서:
 * 1. JwtAuthenticationFilter에서 토큰 검증 실패
 * 2. SecurityConfig에서 설정한 AuthenticationEntryPoint 호출
 * 3. 이 클래스의 commence() 메서드 실행
 * 4. 표준화된 401 에러 응답 반환
 */
@Slf4j
@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    // Spring이 자동 설정한 ObjectMapper Bean을 주입받음
    private final ObjectMapper objectMapper;

    public JwtAuthenticationEntryPoint(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * 인증 실패시 호출되는 메서드
     * +
     * Spring Security가 인증되지 않은 요청을 감지했을 때 자동으로 호출됨
     * 클라이언트에게 표준화된 401 에러 응답을 반환
     *
     * @param request 인증 실패한 HTTP 요청
     * @param response HTTP 응답 객체 (에러 응답 작성용)
     * @param authException 발생한 인증 예외
     * @throws IOException 응답 작성 중 입출력 오류 발생시
     */
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException {

        // 보안 모니터링을 위한 인증 실패 로그 기록
        // 요청 URI와 예외 메시지를 함께 기록하여 보안 분석 시 사용
        log.error("Unauthorized access attempt - URI: {}, Error: {}", request.getRequestURI(), authException.getMessage());

        // HTTP 응답 헤더 설정
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);  // JSON 응답 타입 설정
        response.setCharacterEncoding("UTF-8"); // 한글 인코딩 설정
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);    // 401 상태 코드 설정

        // 표준화된 API 에러 응답 생성
        ApiResponse<Object> apiResponse = ApiResponse.error(
                "UNAUTHORIZED", // 에러 코드 (클라이언트에서 에러 타입 구분용)
                "인증이 필요합니다. 로그인 후 다시 시도해주세요."   // 사용자 친화적 에러 메시지
        );

        // Java 객체를 JSON 문자열로 변환
        String jsonResponse = objectMapper.writeValueAsString(apiResponse);

        // HTTP 응답 바디에 JSON 데이터 작성
        response.getWriter().write(jsonResponse);
    }
}
