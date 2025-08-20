package com.moviebuddies.config;

import com.moviebuddies.websocket.StompAuthChannelInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * WebSocket 설정 클래스
 * JWT 인증 기반 실시간 채팅을 위한 STOMP over WebSocket 설정
 * 간단한 채팅 시스템을 위한 메시지 브로커 및 인증 구성
 */
@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final StompAuthChannelInterceptor stompAuthChannelInterceptor;

    /**
     * STOMP 메시지 브로커 설정
     * 클라이언트 - 서버 간 메시지 라우팅 규칙 정의
     * 
     * @param config 메시지 브로커 레지스트리 설정 객체
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {

        // 서버 -> 클라이언트 브로드캐스트용 (구독 경로)
        //   `/topic/chat/{roomId}` : 채팅방별 메시지 브로드캐스트
        config.enableSimpleBroker("/topic");

        // 클라이언트 -> 서버 메시지 전송용 (발행 경로)
        //   `/app/chat/{roomId}/send` : 메시지 전송
        //   `/app/chat/{roomId}/join` : 채팅방 입장
        //   `/app/chat/{roomId}/leave` : 채팅방 퇴장
        config.setApplicationDestinationPrefixes("/app");
    }

    /**
     * WebSocket 연결 엔드포인트 설정
     * 클라이언트가 최초 WebSocket 연결을 시도할 때 사용할 경로
     * - 엔드포인트: /ws
     * - SockJS fallback 지원으로 브라우저 호환성 보장
     * - CORS 전체 허용 (운영 시에는 구체적인 도메인을 설정하는 것이 권장됨)
     *
     * @param registry STOMP 엔드포인트 레지스트리 설정 객체
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {

        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*") // CORS 허용 (개발용으로 허용함)
                .withSockJS(); // SockJS fallback 지원 (WebSocket 미지원 브라우저 대응)
    }

    /**
     * 클라이언트 인바운드 채널 설정
     * 클라이언트에서 서버로 오는 메시지에 대한 인터셉터 등록
     * - JWT 토큰 기반 인증: 클라이언트 CONNECT 시 JWT 토큰 검증
     *     인증 성공 -> WebSocket 연결 허용 및 사용자 정보 설정
     *     인증 실패 -> 연결 차단
     *
     * @param registration 클라이언트 인바운드 채널 설정 객체
     */
    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {

        // JWT 기반 WebSocket 연결 인증 인터셉터 적용
        registration.interceptors(stompAuthChannelInterceptor);
    }
}
