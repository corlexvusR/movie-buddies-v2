package com.moviebuddies.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT 인증 필터 클래스
 * +
 * 주요 기능:
 * - 모든 HTTP 요청에서 JWT 토큰 추출 및 검증
 * - Authorization 헤더에서 Bearer 토큰 추출
 * - WebSocket 연결을 위한 쿼리 파라미터 토큰 지원
 * - 유효한 토큰일 경우 Spring Security Context에 인증 정보 설정
 * - 한 번의 요청당 한 번만 실행되도록 보장 (OncePerRequestFilter 상속)
 * +
 * 필터 실행 순서:
 * 1. HTTP 요청에서 JWT 토큰 추출
 * 2. 토큰 유효성 검증
 * 3. 토큰에서 사용자명 추출
 * 4. 사용자 정보 로드
 * 5. Security Context에 인증 정보 설정
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    // JWT 토큰 생성/검증을 담당하는 유틸리티
    private final JwtUtil jwtUtil;
    // 사용자 정보를 로드하는 서비스
    private final UserDetailsService userDetailsService;

    /**
     * 모든 HTTP 요청에 대해 JWT 인증을 수행하는 메인 필터 메서드
     * +
     * 처리 과정:
     * 1. 요청에서 JWT 토큰 추출
     * 2. 토큰이 존재하고 유효한지 검증
     * 3. 토큰에서 사용자명 추출
     * 4. 데이터베이스에서 사용자 정보 로드
     * 5. 토큰과 사용자 정보 재검증
     * 6. Security Context에 인증 정보 설정
     * 7. 다음 필터로 요청 전달
     *
     * @param request HTTP 요청 객체
     * @param response HTTP 응답 객체
     * @param filterChain 필터 체인 (다음 필터로 요청을 전달하는 용도)
     * @throws ServletException 서블릿 예외
     * @throws IOException 입출력 예외
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        // 1. 요청에서 JWT 토큰 추출
        String jwt = getJwtFromRequest(request);

        // 2. 토큰이 존재하고 기본적으로 유효한지 확인
        if (StringUtils.hasText(jwt) && jwtUtil.validateToken(jwt)) {
            // 3. 토큰에서 사용자명 추출
            String username = jwtUtil.getUsernameFromToken(jwt);

            // 4. 사용자명으로 데이터베이스에서 사용자 상세 정보 로드
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            // 5. 토큰과 사용자 정보를 함께 재검증 (보안 강화)
            if (jwtUtil.validateToken(jwt, userDetails)) {
                // 6. Spring Security 인증 토큰 생성
                // userDetails: 인증된 사용자 정보
                // null: 비밀번호 (JWT에서는 사용하지 않음)
                // userDetails.getAuthorities(): 사용자 권한 목록
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                // 7. 요청 세부 정보 설정 (IP 주소, 세션 ID 등)
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                // 8. Security Context에 인증 정보 저장
                // 이후 @AuthenticationPrincipal, SecurityContextHolder 등으로 접근 가능
                SecurityContextHolder.getContext().setAuthentication(authentication);

                log.debug("Security context set for user: {}", username);
            }
        }
        // 9. 다음 필터로 요청 전달 (필터 체인 계속 실행)
        filterChain.doFilter(request, response);
    }

    /**
     * HTTP 요청에서 JWT 토큰을 추출하는 메서드
     * +
     * 지원하는 토큰 전달 방식:
     * 1. Authentication 헤더 (일반 API 호출): "Bearer {token}"
     * 2. 쿼리 파라미터 (WebSocket 연결): "?token={token}"
     *
     * @param request HTTP 요청 객체
     * @return String JWT 토큰 문자열 (토큰이 없으면 null)
     */
    private String getJwtFromRequest(HttpServletRequest request) {

        // 1. Authentication 헤더에서 Bearer 토큰 추출 (일반적인 방식)
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            // "Bearer " 부분(7글자)을 제거하고 실제 토큰 반환
            return bearerToken.substring(7);
        }

        // 2. WebSocket 연결의 경우 쿼리 파라미터에서 토큰 추출
        // WebSocket은 헤더 설정이 제한적이므로 URL 파라미터 사용
        // 예: ws://localhost:8080/ws/chat?token=aBcDe0...
        String tokenParam = request.getParameter("token");
        if (StringUtils.hasText(tokenParam)) {
            return tokenParam;
        }
        
        // 3. 토큰을 찾을 수 없는 경우 null 반환
        return null;
    }
}
