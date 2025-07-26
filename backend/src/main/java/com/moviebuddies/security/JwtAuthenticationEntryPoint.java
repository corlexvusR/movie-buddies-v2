package com.moviebuddies.security;

import com.fasterxml.jackson.databind.ObjectMapper;
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
 */
@Slf4j
@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    // JSON 응답 생성을 위한 ObjectMapper
    private final ObjectMapper objectMapper = new ObjectMapper();

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

        // 인증 실패 로그 기록 (보안 모니터링용)
        log.error("Unauthorized error: {}", authException.getMessage();

        // HTTP 응답 설정
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);  // JSON 응답 타입 설정
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);    // 401 상태 코드 설정

        /*
          표준화된 에러 응답 JSON 생성과 HTTP 응답 바디에 작성 (추후 구현)
         */
    }
}
