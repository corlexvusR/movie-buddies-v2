package com.moviebuddies.websocket;

import com.moviebuddies.security.JwtUtil;
import com.moviebuddies.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

/**
 * WebSocket 연결 시 JWT 토큰 기반 인증을 처리하는 인터셉터
 * STOMP CONNECT 명령어 시에만 인증을 수행하며, 유효한 토큰이 있는 경우에만 연결을 허용
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class StompAuthChannelInterceptor implements ChannelInterceptor {

    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;

    /**
     * 메시지 전송 전 인증 처리
     * STOMP CONNECT 명령어에 대해서만 JWT 토큰 검증 수행
     *
     * @param message 전송될 메시지
     * @param channel 메시지 채널
     * @return 인증 성공 시 원본 메시지, 실패 시 null (연결 차단)
     */
    @Override
    @Nullable
    public Message<?> preSend(@NonNull Message<?> message, @NonNull MessageChannel channel) {

        // StompHeaderAccessor 안전하게 추출
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        // accessor가 null 이거나 CONNECT 명령어가 아닌 경우 그대로 통과
        if (accessor == null || !StompCommand.CONNECT.equals(accessor.getCommand())) {
            return message;
        }

        // JWT 토큰 추출 시도
        String token = extractToken(accessor);

        // 토큰이 없거나 유효하지 않은 경우 연결 차단
        if (token == null || !jwtUtil.validateToken(token)) {
            log.warn("WebSocket 연결 시 유효하지 않은 토큰 - 세션: {}",
                    accessor.getSessionId());
            return null;
        }

        // 토큰이 유효한 경우 사용자 인증 정보 설정
        try {
            String username = jwtUtil.getUsernameFromToken(token);
            UserDetailsImpl userDetails = (UserDetailsImpl) userDetailsService.loadUserByUsername(username);

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

            accessor.setUser(authentication);
            log.info("WebSocket 인증 성공 - 사용자: {}, 세션: {}",
                    username, accessor.getSessionId());

            return message;

        } catch (Exception e) {
            log.error("WebSocket 인증 처리 중 오류 발생 - 세션: {}, 오류: {}",
                    accessor.getSessionId(), e.getMessage());
            return null;
        }
    }

    /**
     * HTTP 헤더 또는 URL 파라미터에서 JWT 토큰 추출
     * 우선순위: Authorization 헤더 > token 파라미터
     *
     * @param accessor STOMP 헤더 접근자
     * @return 추출된 토큰 문자열 (Bearer 접두사 제거된 상태), 없으면 null
     */
    @Nullable
    private String extractToken(@NonNull StompHeaderAccessor accessor) {

        // 1. Authorization 헤더에서 토큰 추출 시도
        String token = accessor.getFirstNativeHeader("Authorization");

        // 2. Authorization 헤더가 없으면 token 파라미터에서 추출 시도
        if (token == null) {
            token = accessor.getFirstNativeHeader("token");
        }

        // 3. Bearer 접두사 제거
        if (token != null && token.startsWith("Bearer ")) {
            return token.substring(7);
        }

        return token;
    }
}