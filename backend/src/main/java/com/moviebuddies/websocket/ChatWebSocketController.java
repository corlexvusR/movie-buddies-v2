package com.moviebuddies.websocket;

import com.moviebuddies.dto.request.ChatMessageRequest;
import com.moviebuddies.dto.response.ChatMessageResponse;
import com.moviebuddies.security.UserDetailsImpl;
import com.moviebuddies.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Controller;

/**
 * 채팅 WebSocket 컨트롤러
 * 간소화된 실시간 채팅 기능 제공
 * JWT 인증을 통해 인증된 사용자만 접근 가능하며 안전한 인증 정보 추출 보장
 */
@Slf4j
@Controller
@RequiredArgsConstructor
public class ChatWebSocketController {

    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * 채팅 메시지 전송
     * 실시간으로 채팅방 내 모든 참가자에게 메시지 브로드캐스트
     *
     * @param roomId 채팅방 ID
     * @param request 메시지 내용
     * @param headerAccessor WebSocket 헤더 접근자
     * @return 전송된 메시지 정보
     * @throws RuntimeException 인증 실패 또는 메시지 전송 실패 시
     */
    @MessageMapping("/chat/{roomId}/send")
    @SendTo("/topic/chat/{roomId}")
    public ChatMessageResponse sendMessage(
            @DestinationVariable String roomId,
            ChatMessageRequest request,
            SimpMessageHeaderAccessor headerAccessor) {

        try {
            UserDetailsImpl userDetails = getAuthenticatedUser(headerAccessor);
            log.info("채팅 메시지 수신 - 방: {}, 사용자: {}", roomId, userDetails.getUsername());

            Long chatRoomId = Long.parseLong(roomId);
            ChatMessageResponse response = chatService.sendMessage(userDetails.getId(), chatRoomId, request);

            log.info("채팅 메시지 전송 완료 - 메시지 ID: {}", response.getId());
            return response;

        } catch (Exception e) {
            log.error("채팅 메시지 전송 실패 - 방: {}, 오류: {}", roomId, e.getMessage());
            throw new RuntimeException("메시지 전송에 실패했습니다.", e);
        }
    }

    /**
     * 채팅방 입장
     * 실시간으로 입장 알림 메시지 전송
     *
     * @param roomId 입장할 채팅방 ID
     * @param headerAccessor WebSocket 헤더 접근자
     */
    @MessageMapping("/chat/{roomId}/join")
    public void joinRoom(
            @DestinationVariable String roomId,
            SimpMessageHeaderAccessor headerAccessor) {

        try {
            UserDetailsImpl userDetails = getAuthenticatedUser(headerAccessor);
            log.info("채팅방 입장 - 방: {}, 사용자: {}", roomId, userDetails.getUsername());

            Long chatRoomId = Long.parseLong(roomId);
            chatService.joinRoom(userDetails.getId(), chatRoomId);

            // 입장 알림 메시지 전송
            ChatMessageResponse joinMessage = ChatMessageResponse.createSystemMessage(
                    chatRoomId, "새로운 참가자가 입장하셨습니다.");

            messagingTemplate.convertAndSend("/topic/chat/" + roomId, joinMessage);

        } catch (Exception e) {
            log.error("채팅방 입장 실패 - 방: {}, 오류: {}", roomId, e.getMessage());
        }
    }

    /**
     * 채팅방 퇴장
     * 실시간으로 퇴장 알림 메시지 전송
     *
     * @param roomId 퇴장할 채팅방 ID
     * @param headerAccessor WebSocket 헤더 접근자
     */
    @MessageMapping("/chat/{roomId}/leave")
    public void leaveRoom(
            @DestinationVariable String roomId,
            SimpMessageHeaderAccessor headerAccessor) {

        try {
            UserDetailsImpl userDetails = getAuthenticatedUser(headerAccessor);
            log.info("채팅방 퇴장 - 방: {}, 사용자: {}", roomId, userDetails.getUsername());

            Long chatRoomId = Long.parseLong(roomId);
            chatService.leaveRoom(userDetails.getId(), chatRoomId);

            // 익명 퇴장 알림 메시지 전송
            ChatMessageResponse leaveMessage = ChatMessageResponse.createSystemMessage(
                    chatRoomId, "참가자가 퇴장하셨습니다.");

            messagingTemplate.convertAndSend("/topic/chat/" + roomId, leaveMessage);

        } catch (Exception e) {
            log.error("채팅방 퇴장 실패 - 방: {}, 오류: {}", roomId, e.getMessage());
        }
    }

    /**
     * WebSocket 헤더에서 인증된 사용자 정보 안전하게 추출
     * NPE 방지를 위한 다단계 검증 수행
     *
     * @param headerAccessor WebSocket 메시지 헤더 접근자
     * @return 인증된 사용자 정보
     * @throws RuntimeException 인증 정보가 없거나 유효하지 않은 경우
     */
    private UserDetailsImpl getAuthenticatedUser(SimpMessageHeaderAccessor headerAccessor) {
        // 1단계: 인증 객체 존재 여부 확인
        if (headerAccessor.getUser() == null) {
            log.error("WebSocket 연결에 인증 정보가 없음 - 세션: {}",
                    headerAccessor.getSessionId());
            throw new RuntimeException("인증이 필요합니다.");
        }

        // 2단계: 인증 토큰 타입 확인
        if (!(headerAccessor.getUser() instanceof UsernamePasswordAuthenticationToken auth)) {
            log.error("올바르지 않은 인증 토큰 타입 - 세션: {}, 타입: {}",
                    headerAccessor.getSessionId(),
                    headerAccessor.getUser().getClass().getSimpleName());
            throw new RuntimeException("올바르지 않은 인증 정보입니다.");
        }

        // 3단계: Principal 객체 존재 및 타입 확인
        if (auth.getPrincipal() == null || !(auth.getPrincipal() instanceof UserDetailsImpl userDetails)) {
            log.error("사용자 정보가 없거나 올바르지 않음 - 세션: {}",
                    headerAccessor.getSessionId());
            throw new RuntimeException("사용자 정보를 찾을 수 없습니다.");
        }

        // 4단계: 사용자 정보 유효성 확인
        if (userDetails.getId() == null || userDetails.getUsername() == null) {
            log.error("불완전한 사용자 정보 - 세션: {}", headerAccessor.getSessionId());
            throw new RuntimeException("사용자 정보가 불완전합니다.");
        }

        return userDetails;
    }
}