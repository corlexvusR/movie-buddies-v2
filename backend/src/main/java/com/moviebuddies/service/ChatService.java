package com.moviebuddies.service;

import com.moviebuddies.dto.request.ChatMessageRequest;
import com.moviebuddies.dto.request.ChatRoomCreateRequest;
import com.moviebuddies.dto.response.ChatMessageResponse;
import com.moviebuddies.dto.response.ChatRoomResponse;
import com.moviebuddies.entity.ChatMessage;
import com.moviebuddies.entity.ChatRoom;
import com.moviebuddies.entity.User;
import com.moviebuddies.exception.BusinessException;
import com.moviebuddies.exception.ResourceNotFoundException;
import com.moviebuddies.repository.ChatMessageRepository;
import com.moviebuddies.repository.ChatRoomRepository;
import com.moviebuddies.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 채팅 서비스
 * 익명 채팅 시스템의 핵심 비즈니스 로직 담당
 * 채팅방 관리, 메시지 처리, 참가자 관리 등을 포함
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final UserRepository userRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * Redis 키 패턴: 채팅방별 활성 사용자 목록 저장
     * 실시간 접속자 수 추적 및 관리용
     */
    private static final String ACTIVE_USERS_KEY = "chat:active_users";

    /**
     * 새로운 채팅방 생성
     * 생성자는 자동으로 해당 채팅방에 참가하게 됨
     *
     * @param userId 채팅방 생성자 ID
     * @param request 채팅방 생성 정보
     * @return 생성된 채팅방 정보 (익명화된 형태)
     * @throws ResourceNotFoundException 존재하지 않는 사용자인 경우
     * @throws BusinessException 이미 존재하는 채팅방 이름인 경우
     */
    @Transactional
    public ChatRoomResponse createRoom(Long userId, ChatRoomCreateRequest request) {

        log.info("채팅방 생성 - 사용자 ID: {}, 방 이름: {}", userId, request.getName());

        User creator = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("사용자", userId));

        // 중복 이름 확인: 채팅방 이름은 고유해야 함
        if (chatRoomRepository.existsByName(request.getName())) {
            throw BusinessException.conflict("이미 존재하는 채팅방 이름입니다.");
        }

        ChatRoom chatRoom = ChatRoom.builder()
                .name(request.getName())
                .description(request.getDescription())
                .maxParticipants(request.getMaxParticipants())
                .createdBy(creator)
                .build();
        
        // 생성자를 자동으로 첫번째 참가자로 추가
        chatRoom.joinRoom(creator);

        ChatRoom savedRoom = chatRoomRepository.save(chatRoom);
        log.info("채팅방 생성 완료 - 방 ID: {}", savedRoom.getId());

        return ChatRoomResponse.from(savedRoom);
    }

    /**
     * 전체 활성 채팅방 목록 조회 (페이징)
     * 메인 채팅방 목록 화면에서 사용
     *
     * @param pageable 페이징 정보
     * @return 활성 채팅방 목록
     */
    @Cacheable(value = "chatRooms", key = "#pageable.pageNumber + '_' + #pageable.pageSize")
    public Page<ChatRoomResponse> getChatRooms(Pageable pageable) {

        log.info("채팅방 조회");

        Page<ChatRoom> chatRooms = chatRoomRepository.findByIsActiveTrueOrderByCreatedAtDesc(pageable);

        return chatRooms.map(ChatRoomResponse::from);
    }

    /**
     * 특정 사용자가 참여한 채팅방 목록 조회
     * 사용자의 개인 채팅방 목록 화면에서 사용
     *
     * @param userId 조회할 사용자 ID
     * @return 해당 사용자가 참여한 활성 채팅방 목록
     */
    @Cacheable(value = "userChatRooms", key = "#userId")
    public List<ChatRoomResponse> getUserChatRooms(Long userId) {

        log.info("사용자 참여 채팅방 목록 조회 - 사용자 ID: {}", userId);

        List<ChatRoom> chatRooms = chatRoomRepository.findByParticipantsIdAndIsActiveTrue(userId);
        return chatRooms.stream()
                .map(ChatRoomResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 특정 채팅방의 상세 정보 조회
     * 채팅방 입장 전 정보 확인용
     *
     * @param roomId 조회할 채팅방 ID
     * @return 채팅방 상세 정보 (익명화된 형태)
     * @throws ResourceNotFoundException 존재하지 않는 채팅방인 경우
     */
    @Cacheable(value = "chatRoomDetail", key = "#roomId")
    public ChatRoomResponse getChatRoomDetail(Long roomId) {

        log.info("채팅방 상세 정보 조회 - 방 ID: {}", roomId);

        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("채팅방", roomId));

        return ChatRoomResponse.from(chatRoom);
    }

    /**
     * 채팅방 입장 처리
     * 참가자 목록에 추가하고 Redis에 활성 사용자로 등록
     * 캐시 무효화로 실시간 참가자 수 업데이트
     *
     * @param userId 입장할 사용자 ID
     * @param roomId 입장할 채팅방 ID
     * @throws ResourceNotFoundException 존재하지 않는 사용자/채팅방인 경우
     * @throws BusinessException 입장 조건을 만족하지 않는 경우
     */
    @Transactional
    @CacheEvict(value = {"chatRoomDetail", "userChatRooms"}, allEntries = true)
    public void joinRoom(Long userId, Long roomId) {

        log.info("채팅방 입장 - 사용자 ID: {}, 방 ID: {}", userId, roomId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("사용자", userId));

        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("채팅방", roomId));

        // 입장 가능 여부 확인 (최대 인원 제한, 중복 참가 제한 등)
        if (!chatRoom.joinRoom(user)) {
            throw BusinessException.badRequest("채팅방에 입장할 수 없습니다.");
        }

        chatRoomRepository.save(chatRoom);
        
        // Redis에 활성 사용자로 등록
        addActiveUser(roomId, userId);

        log.info("채팅방 입장 완료 - 사용자 ID: {}, 방 ID: {}", userId, roomId);
    }

    /**
     * 채팅방 퇴장 처리
     * 참가자 목록에서 제거하고 Redis에서 활성 사용자 해제
     * 캐시 무효화로 실시간 참가자 수 업데이트
     * 
     * @param userId 퇴장할 사용자 ID
     * @param roomId 퇴장할 채팅방 ID
     * @throws ResourceNotFoundException 존재하지 않는 사용자/채팅방인 경우
     * @throws BusinessException 퇴장할 수 없는 경우 (참가하지 않은 채팅방)
     */
    @Transactional
    @CacheEvict(value = {"chatRoomDetail", "userChatRooms"}, allEntries = true)
    public void leaveRoom(Long userId, Long roomId) {

        log.info("채팅방 퇴장 - 사용자 ID: {}, 방 ID: {}", userId, roomId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("사용자", userId));

        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("채팅방", roomId));

        // 퇴장 가능 여부 확인 (사용자가 기존에 참가했는지)
        if (!chatRoom.leaveRoom(user)) {
            throw BusinessException.badRequest("채팅방에 퇴장할 수 없습니다.");
        }

        chatRoomRepository.save(chatRoom);

        // Redis에서 활성 사용자 제거
        removeActiveUser(roomId, userId);

        log.info("채팅방 퇴장 완료 - 사용자 ID: {}, 방 ID: {}", userId, roomId);
    }

    /**
     * 채팅 메시지 전송 처리
     * 참가자 권한 확인 후 메시지를 데이터베이스에 저장
     * 실시간 WebSocket 전송은 별도 컨트롤러에서 처리
     *
     * @param userId 메시지 발송자 ID
     * @param roomId 메시지를 보낼 채팅방 ID
     * @param request 메시지 내용
     * @return 저장된 메시지 정보 (익명화된 형태)
     * @throws ResourceNotFoundException 존재하지 않는 사용자/채팅방인 경우
     * @throws BusinessException 메시지 전송 권한이 없는 경우 (비참가자)
     */
    @Transactional
    public ChatMessageResponse sendMessage(Long userId, Long roomId, ChatMessageRequest request) {

        log.info("메시지 전송 - 사용자 ID: {}, 방 ID: {}", userId, roomId);

        User sender = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("사용자", userId));

        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("채팅방", roomId));

        // 참가자 권한 확인 (참가자만 메시지 전송 가능)
        if (!chatRoom.isParticipant(sender)) {
            throw BusinessException.forbidden("채팅방 참가자만 메시지를 보낼 수 있습니다.");
        }

        ChatMessage message = ChatMessage.builder()
                .chatRoom(chatRoom)
                .sender(sender)
                .content(request.getContent())
                .build();

        ChatMessage savedMessage = chatMessageRepository.save(message);

        log.info("메시지 전송 완료 - 메시지 ID: {}", savedMessage.getId());

        return ChatMessageResponse.from(savedMessage);
    }

    /**
     * 채팅방 메시지 히스토리 조회
     * 실시간 채팅 화면 진입 시 이전 대화 내용 로드용
     * 참가자만 조회 가능하도록 권한 확인
     *
     * @param roomId 조회할 채팅방 ID
     * @param pageable 페이징 정보 (최근 50개)
     * @return 해당 채팅방의 메시지 목록 (익명화된 형태, 최신순)
     * @throws ResourceNotFoundException 존재하지 않는 채팅방인 경우
     * @throws BusinessException 메시지 조회 권한이 없는 경우 (비참가자)
     */
    @Cacheable(value = "chatMessages", key = "#userId + '_' + #roomId + '_' + #pageable.pageNumber + '_' + #pageable.pageSize")
    public Page<ChatMessageResponse> getChatMessages(Long userId, Long roomId, Pageable pageable) {

        log.info("채팅방 메시지 조회 - 사용자 ID: {}, 방 ID: {}", userId, roomId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("사용자", userId));

        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("채팅방", roomId));

        // 참가자 권한 확인 (참가자만 메시지 조회 가능)
        if (!chatRoom.isParticipant(user)) {
            throw BusinessException.forbidden("채팅방 참가자만 대화 기록을 조회할 수 있습니다.");
        }

        Page<ChatMessage> messages = chatMessageRepository.findByChatRoomIdOrderByCreatedAtDesc(roomId, pageable);

        return messages.map(ChatMessageResponse::from);
    }

    /**
     * 채팅방 현재 활성 사용자 목록 조회
     * 실시간 접속자 수 표시용
     *
     * @param roomId 조회할 채팅방 ID
     * @return 현재 활성 사용자 ID 목록
     */
    public Set<Object> getActiveUsers(Long roomId) {

        String key = ACTIVE_USERS_KEY + roomId;

        return redisTemplate.opsForSet().members(key);
    }

    /**
     * Redis에 활성 사용자 추가
     * 채팅방 입장 시 호출하여 실시간 접속자 추적
     *
     * @param roomId 채팅방 ID
     * @param userId 사용자 ID
     */
    private void addActiveUser(Long roomId, Long userId) {

        String key = ACTIVE_USERS_KEY + roomId;

        redisTemplate.opsForSet().add(key, userId);
        
        // 30분 후 자동 만료 (비정상 종료 대비)
        redisTemplate.expire(key, 30, TimeUnit.MINUTES);
    }

    /**
     * Redis에서 활성 사용자 제거
     * 채팅방 퇴장 시 호출하여 실시간 접속자에서 제외
     *
     * @param roomId 채팅방 ID
     * @param userId 사용자 ID
     */
    private void removeActiveUser(Long roomId, Long userId) {

        String key = ACTIVE_USERS_KEY + roomId;

        redisTemplate.opsForSet().remove(key, userId);
    }
}
