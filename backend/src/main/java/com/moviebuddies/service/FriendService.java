package com.moviebuddies.service;

import com.moviebuddies.dto.response.FriendResponse;
import com.moviebuddies.entity.Friend;
import com.moviebuddies.entity.User;
import com.moviebuddies.exception.BusinessException;
import com.moviebuddies.exception.ResourceNotFoundException;
import com.moviebuddies.repository.FriendRepository;
import com.moviebuddies.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 친구 관리 서비스
 *
 * 친구 시스템의 핵심 비즈니스 로직을 처리
 * 양방향 친구 관계의 복잡성을 관리하고, 친구 요청의 전체 생명주기를 담당
 *
 * 주요 기능:
 * - 친구 요청 생성/수락/거절
 * - 친구 목록 조회 (다양한 형태별)
 * - 친구 관계 삭제
 * - 중복 요청 방지 및 유효성 검증
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true) // 기본적으로 읽기 전용, 쓰기 작업에만 @Transactional 추가
public class FriendService {

    private final FriendRepository friendRepository;
    private final UserRepository userRepository;

    /**
     * 친구 요청 보내기
     *
     * 새로운 친구 요청을 생성
     * 다양한 유효성 검증을 수행하여 중복 요청이나 잘못된 요청을 방지
     *
     * 검증 항목:
     * - 자기 자신에게 요청 방지
     * - 이미 친구인 경우 방지
     * - 이미 대기 중인 요청이 있는 경우 방지
     *
     * @param requesterId 요청자 사용자 ID
     * @param receiverUsername 수신자 사용자명
     * @return 생성된 친구 요청 정보
     * @throws ResourceNotFoundException 사용자를 찾을 수 없는 경우
     * @throws BusinessException 비즈니스 규칙 위반 시
     */
    @Transactional  // 쓰기 작업이므로 트랜잭션 적용
    public FriendResponse sendFriendRequest(Long requesterId, String receiverUsername) {
        log.info("친구 요청 - 요청자 ID: {}, 수신자: {}", requesterId, receiverUsername);

        // 사용자 존재 확인
        User requester = userRepository.findById(requesterId)
                .orElseThrow(() -> new ResourceNotFoundException("요청자", requesterId));

        User receiver = userRepository.findByUsername(receiverUsername)
                .orElseThrow(() -> new ResourceNotFoundException("수신자", "username", receiverUsername));

        // 자기 자신에게 요청 방지
        if (requester.getId().equals(receiver.getId())) {
            throw BusinessException.badRequest("자기 자신에게 친구 요청을 보낼 수 없습니다.");
        }

        // 기존 관계 확인 (중복 방지)
        Optional<Friend> existingFriendship = friendRepository.findFriendshipBetweenUsers(requester.getId(), receiver.getId());

        if (existingFriendship.isPresent()) {
            Friend existing = existingFriendship.get();
            if (existing.getStatus() == Friend.FriendStatus.ACCEPTED) {
                throw BusinessException.conflict("이미 친구인 사용자입니다.");
            } else if (existing.getStatus() == Friend.FriendStatus.PENDING) {
                throw BusinessException.conflict("이미 친구 요청이 존재합니다.");
            }
        }

        // 새로운 친구 요청 생성
        Friend friendRequest = Friend.builder()
                .requester(requester)
                .receiver(receiver)
                .status(Friend.FriendStatus.PENDING)
                .build();

        Friend savedRequest = friendRepository.save(friendRequest);
        log.info("친구 요청 완료 - 요청 ID: {}", savedRequest.getId());

        return FriendResponse.from(savedRequest, requester);
    }

    /**
     * 친구 요청 수락
     *
     * 받은 친구 요청을 수락하여 친구 관계를 확정
     * 상태를 PENDING에서 ACCEPTED로 변경하고 수락 시간을 기록
     *
     * @param receiverId 수신자(현재 사용자) ID
     * @param requesterUsername 요청자 사용자명
     * @throws ResourceNotFoundException 친구 요청을 찾을 수 없는 경우
     */
    @Transactional  // 쓰기 작업이므로 트랜잭션 적용
    public void acceptFriendRequest(Long receiverId, String requesterUsername) {
        log.info("친구 요청 수락 - 수신자 ID: {}, 요청자: {}", receiverId, requesterUsername);

        // 대기 중인 친구 요청 조회
        Friend friendRequest = friendRepository.findPendingRequestByUsernameAndReceiver(requesterUsername, receiverId)
                .orElseThrow(() -> new ResourceNotFoundException("친구 요청을 찾을 수 없습니다."));

        // 요청 수락 처리 (상태 변경 및 수락 시간 기록)
        friendRequest.accept();;
        friendRepository.save(friendRequest);

        log.info("친구 요청 수락 완료 - 요청 ID: {}", friendRequest.getId());
    }

    /**
     * 친구 요청 거절
     *
     * 받은 친구 요청을 거절
     * 거절된 요청은 데이터베이스에서 완전히 삭제
     * (거절 기록을 남기고 싶다면 상태를 DECLINED로 변경하는 방식도 가능)
     *
     * @param receiverId 수신자(현재 사용자) ID
     * @param requesterUsername 요청자 사용자명
     * @throws ResourceNotFoundException 친구 요청을 찾을 수 없는 경우
     */
    @Transactional  // 쓰기 작업이므로 트랜잭션 적용
    public void declineFriendRequest(Long receiverId, String requesterUsername) {
        log.info("친구 요청 거절 - 수신자 ID: {}, 요청자: {}", receiverId, requesterUsername);

        // 대기 중인 친구 요청 조회
        Friend friendRequest = friendRepository.findPendingRequestByUsernameAndReceiver(requesterUsername, receiverId)
                .orElseThrow(() -> new ResourceNotFoundException("친구 요청을 찾을 수 없습니다."));

        // 요청 삭제 (거절 시 기록을 남기지 않음)
        friendRepository.delete(friendRequest);

        log.info("친구 요청 거절 완료 - 요청 ID: {}", friendRequest.getId());
    }

    /**
     * 친구 목록 조회
     *
     * 현재 사용자의 수락된 친구 관계 목록을 조회
     * 양방향 친구 관계에서 현재 사용자를 기준으로 상대방 정보를 반환
     *
     * @param userId 조회할 사용자 ID
     * @return 친구 목록 (상대방 사용자 정보 포함)
     * @throws ResourceNotFoundException 사용자를 찾을 수 없는 경우
     */
    public List<FriendResponse> getFriends(Long userId) {
        log.info("친구 목록 조회 - 사용자 ID: {}", userId);

        // 사용자 존재 확인
        User user = userRepository.findById(userId)
                        .orElseThrow(() -> new ResourceNotFoundException("사용자", userId));

        // 수락된 친구 관계 조회
        List<Friend> friends = friendRepository.findAcceptedFriendsByUserId(userId);

        // DTO 변환 (현재 사용자 기준으로 상대방 정보 추출)
        return friends.stream()
                .map(friend -> FriendResponse.from(friend, user))
                .collect(Collectors.toList());
    }

    /**
     * 보낸 친구 요청 목록 조회
     *
     * 현재 사용자가 다른 사용자에게 보낸 아직 처리되지 않은 친구 요청 목록을 조회
     * "보낸 요청" 탭에서 요청 취소 기능 등을 위해 사용
     *
     * @param userId 조회할 사용자 ID (요청자)
     * @return 보낸 친구 요청 목록
     * @throws ResourceNotFoundException 사용자를 찾을 수 없는 경우
     */
    public List<FriendResponse> getSentRequests(Long userId) {
        log.info("보낸 친구 요청 목록 조회 - 사용자 ID: {}", userId);

        // 사용자 존재 확인
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("사용자", userId));

        // 보낸 대기 중인 요청 조회
        List<Friend> sentRequests = friendRepository.findSentRequestsByUserId(userId);

        // DTO 변환
        return sentRequests.stream()
                .map(request -> FriendResponse.from(request, user))
                .collect(Collectors.toList());
    }

    /**
     * 받은 친구 요청 목록 조회
     *
     * 현재 사용자가 다른 사용자로부터 받은 아직 처리하지 않은 친구 요청 목록을 조회
     * "받은 요청" 탭에서 수락/거절 버튼과 함께 표시
     *
     * @param userId 조회할 사용자 ID (수신자)
     * @return 받은 친구 요청 목록
     * @throws ResourceNotFoundException 사용자를 찾을 수 없는 경우
     */
    public List<FriendResponse> getReceivedRequests(Long userId) {
        log.info("받은 친구 요청 목록 조회 - 사용자 ID: {}", userId);

        // 사용자 존재 확인
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("사용자", userId));

        // 받은 대기 중인 요청 조회
        List<Friend> receivedRequests = friendRepository.findReceivedRequestsByUserId(userId);

        // DTO 변환
        return receivedRequests.stream()
                .map(request -> FriendResponse.from(request, user))
                .collect(Collectors.toList());
    }

    /**
     * 친구 삭제
     *
     * 기존의 친구 관계를 삭제
     * 양방향 친구 관계가 완전히 제거되어 서로의 친구 목록에서 사라짐
     * 삭제 후에는 다시 친구 요청을 보낼 수 있음
     *
     * @param userId 현재 사용자 ID
     * @param friendUsername 삭제할 친구의 사용자명
     * @throws ResourceNotFoundException 친구나 친구 관계를 찾을 수 없는 경우
     * @throws BusinessException 친구 관계가 아닌 경우
     */
    @Transactional  // 쓰기 작업이므로 트랜잭션 적용
    public void removeFriend(Long userId, String friendUsername) {
        log.info("친구 삭제 - 사용자 ID: {}, 친구: {}", userId, friendUsername);

        // 삭제할 친구 사용자 조회
        User friend = userRepository.findByUsername(friendUsername)
                .orElseThrow(() -> new ResourceNotFoundException("친구", "username", friendUsername));

        // 친구 관계 조회
        Friend friendship = friendRepository.findFriendshipBetweenUsers(userId, friend.getId())
                .orElseThrow(() -> new ResourceNotFoundException("친구 관계를 찾을 수 없습니다."));

        // 수락된 친구 관계인지 확인
        if (friendship.getStatus() != Friend.FriendStatus.ACCEPTED) {
            throw BusinessException.badRequest("친구 관계가 아닙니다.");
        }

        // 친구 관계 삭제
        friendRepository.delete(friendship);

        log.info("친구 삭제 완료 - 사용자 ID: {}, 친구: {}", userId, friendUsername);
    }
}
