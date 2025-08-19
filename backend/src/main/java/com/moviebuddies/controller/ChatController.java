package com.moviebuddies.controller;

import com.moviebuddies.dto.request.ChatRoomCreateRequest;
import com.moviebuddies.dto.response.ApiResponse;
import com.moviebuddies.dto.response.ChatMessageResponse;
import com.moviebuddies.dto.response.ChatRoomResponse;
import com.moviebuddies.security.UserDetailsImpl;
import com.moviebuddies.service.ChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 채팅 REST API 컨트롤러
 * HTTP 기반 채팅방 관리 및 메시지 히스토리 조회 기능 제공
 * WebSocket 실시간 채팅과 상호 보완적으로 동작
 */
@Slf4j
@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor
@Tag(name = "Chat", description = "채팅 관리 API")
public class ChatController {

    private final ChatService chatService;

    /**
     * 새로운 채팅방 생성
     * 생성자는 자동으로 해당 채팅방에 참가됨
     *
     * @param request 채팅방 생성 정보
     * @param userDetails 인증된 사용자 정보 (채팅방 생성자)
     * @return 생성된 채팅방 정보
     */
    @Operation(summary = "채팅방 생성", description = "새로운 공개 채팅방을 생성합니다.")
    @SecurityRequirement(name = "Bearer Authentication")
    @PostMapping("/rooms")
    public ResponseEntity<ApiResponse<ChatRoomResponse>> createRoom(
            @Valid @RequestBody ChatRoomCreateRequest request,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        ChatRoomResponse room = chatService.createRoom(userDetails.getId(), request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("채팅방이 생성되었습니다.", room));
    }

    /**
     * 전체 채팅방 목록 조회 (페이징)
     * 메인 채팅방 목록 화면에서 사용
     * 인증 없이도 접근 가능하여 누구나 채팅방 목록 확인 가능
     * 
     * @param pageable 페이징 정보
     * @return 활성 채팅방 목록 (생성일 기준 내림차순)
     */
    @Operation(summary = "채팅방 목록 조회", description = "모든 공개 채팅방 목록을 페이징하여 조회합니다.")
    @GetMapping("/rooms")
    public ResponseEntity<ApiResponse<Page<ChatRoomResponse>>> getChatRooms(
            @PageableDefault(size = 2) Pageable pageable) {

        Page<ChatRoomResponse> rooms = chatService.getChatRooms(pageable);

        return ResponseEntity.ok(ApiResponse.success("채팅방 목록입니다.", rooms));
    }

    /**
     * 사용자별 참여 채팅방 목록 조회
     * 개인 채팅방 관리 페이지에서 사용
     * 
     * @param userDetails 인증된 사용자 정보
     * @return 해당 사용자가 참여한 채팅방 목록
     */
    @Operation(summary = "내 채팅방 목록", description = "내가 참여한 채팅방 목록을 조회합니다.")
    @SecurityRequirement(name = "Bearer Authentication")
    @GetMapping("/rooms/my")
    public ResponseEntity<ApiResponse<List<ChatRoomResponse>>> getMyChatRooms(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        List<ChatRoomResponse> rooms = chatService.getUserChatRooms(userDetails.getId());

        return ResponseEntity.ok(ApiResponse.success("내 채팅방 목록입니다.", rooms));
    }

    /**
     * 특정 채팅방 상세 정보 조회
     * 채팅방 입장 전 미리보기나 정보 확인용
     * 인증 없이도 접근 가능
     *
     * @param roomId 조회할 채팅방 ID
     * @return 채팅방 상세 정보
     */
    @Operation(summary = "채팅방 상세 정보", description = "특정 채팅방의 상세 정보를 조회합니다.")
    @GetMapping("/rooms/{roomId}")
    public ResponseEntity<ApiResponse<ChatRoomResponse>> getChatRoomDetails(
            @PathVariable Long roomId) {

        ChatRoomResponse room = chatService.getChatRoomDetail(roomId);

        return ResponseEntity.ok(ApiResponse.success("특정 채팅방의 정보입니다.", room));
    }

    /**
     * 채팅방 메시지 히스토리 조회 (페이징)
     * 실시간 채팅 화면 진입 시 이전 대화 내용 로드용
     * 참가자만 조회 가능하며, 스크롤 업 시 추가 페이지 로드 지원
     *
     * @param roomId 조회할 채팅방 ID
     * @param pageable 페이징 정보 (기본 50개, 페이지 번호로 더 많은 메시지 조회 가능)
     * @param userDetails 인증된 사용자 정보 (권한 확인용)
     * @return 해당 채팅방의 메시지 목록 (최신순)
     */
    @Operation(summary = "채팅방 메시지 조회", description = "채팅방의 메시지 히스토리를 페이징하여 조회합니다. 참가자만 조회 가능하며 스크롤링으로 더 많은 메시지를 로드할 수 있습니다.")
    @SecurityRequirement(name = "Bearer Authentication")
    @GetMapping("/rooms/{roomId}/messages")
    public ResponseEntity<ApiResponse<Page<ChatMessageResponse>>> getChatMessages(
            @PathVariable Long roomId,
            @PageableDefault(size = 50) Pageable pageable,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        Page<ChatMessageResponse> messages = chatService.getChatMessages(userDetails.getId(), roomId, pageable);

        return ResponseEntity.ok(ApiResponse.success("채팅 메시지 목록입니다.", messages));
    }

    /**
     * 채팅방 입장 처리
     * DB에 참가자로 등록하고 실시간 채팅 준비
     * WebSocket 연결과 별도로 명시적인 입장 처리
     *
     * @param roomId 입장할 채팅방 ID
     * @param userDetails 인증된 사용자 정보
     * @return 입장 완료 메시지
     */
    @Operation(summary = "채팅방 입장", description = "채팅방에 입장하여 참가자로 등록됩니다.")
    @SecurityRequirement(name = "Bearer Authentication")
    @PostMapping("/rooms/{roomId}/join")
    public ResponseEntity<ApiResponse<Void>> joinRoom(
            @PathVariable Long roomId,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        chatService.joinRoom(userDetails.getId(), roomId);

        return ResponseEntity.ok(ApiResponse.success("채팅방에 입장했습니다."));
    }

    /**
     * 채팅방 퇴장 처리
     * DB에서 참가자 제거하고 WebSocket 연결 해제 준비
     *
     * @param roomId 퇴장할 채팅방 ID
     * @param userDetails 인증된 사용자 정보
     * @return 퇴장 완료 메시지
     */
    @Operation(summary = "채팅방 퇴장", description = "채팅방에서 퇴장하여 참가자에서 제외됩니다.")
    @SecurityRequirement(name = "Bearer Authentication")
    @PostMapping("/rooms/{roomId}/leave")
    public ResponseEntity<ApiResponse<Void>> leaveRoom(
            @PathVariable Long roomId,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        chatService.leaveRoom(userDetails.getId(), roomId);

        return ResponseEntity.ok(ApiResponse.success("채팅방에 퇴장했습니다."));
    }
}
