package com.moviebuddies.repository;

import com.moviebuddies.entity.Friend;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 친구 관계 데이터 접근 계층
 * 
 * 친구 관계와 관련된 데이터베이스 연산을 처리
 * 양방향 친구 관계의 특성을 고려하여 요청자와 수신자 모두를 검색 조건에 포함
 */
@Repository
public interface FriendRepository extends JpaRepository<Friend, Long> {

    /**
     * 특정 사용자의 수락된 친구 관계 조회
     *
     * 사용자가 요청자이거나 수신자인 모든 수락된 친구 관계를 반환
     * 친구 목록 페이지에서 사용
     *
     * @param userId 조회할 사용자 ID
     * @return 수락된 친구 관계 목록
     */
    @Query("SELECT f FROM Friend f WHERE " + "(f.requester.id = :userId OR f.receiver.id = :userId) " + "AND f.status = 'ACCEPTED'")
    List<Friend> findAcceptedFriendsByUserId(@Param("userId") Long userId);

    /**
     * 사용자가 보낸 대기 중인 친구 요청 조회
     *
     * 현재 사용자가 요청자로서 보낸 아직 수락되지 않은 친구 요청들을 반환
     * "보낸 요청" 탭에서 사용
     *
     * @param userId 요청자 사용자 ID
     * @return 보낸 대기 중인 친구 요청 목록
     */
    @Query("SELECT f FROM Friend f WHERE f.requester.id = :userId AND f.status = 'PENDING'")
    List<Friend> findSentRequestsByUserId(@Param("userId") Long userId);

    /**
     * 사용자가 받은 대기 중인 친구 요청 조회
     * 
     * 현재 사용자가 수신자로서 받은 아직 처리하지 않은 친구 요청들을 반환
     * "받은 요청" 탭에서 사용되며, 수락/거절 버튼이 표시
     *
     * @param userId 수신자 사용자 ID
     * @return 받은 대기 중인 친구 요청 목록
     */
    @Query("SELECT f FROM Friend f WHERE f.receiver.id = :userId AND f.status = 'PENDING'")
    List<Friend> findReceivedRequestsByUserId(@Param("userId") Long userId);

    /**
     * 특정 두 사용자 간의 친구 관계 조회
     *
     * 두 사용자 사이의 친구 관계가 있는지 확인
     * 요청자와 수신자의 순서에 관계없이 검색
     * 중복 친구 요청 방지에 사용
     *
     * @param userId1 첫번째 사용자 ID
     * @param userId2 두번째 사용자 ID
     * @return 친구 관계 (존재하지 않으면 Optional.empty())
     */
    @Query("SELECT f FROM Friend f WHERE " + "(f.requester.id = :userId1 AND f.receiver.id = :userId2) OR " + "(f.requester.id = :userId2 AND f.receiver.id = :userId1)")
    Optional<Friend> findFriendshipBetweenUsers(@Param("userId1") Long userId1, @Param("userId2") Long userId2);

    /**
     * 특정 사용자명으로부터의 친구 요청 조회
     *
     * 사용자명을 통해 특정 사용자로부터 받은 대기 중인 친구 요청을 조회
     * 친구 요청 처리 시 사용
     *
     * @param username 요청자의 사용자명
     * @param receiverId 수신자의 사용자 ID
     * @return 해당 친구 요청 (존재하지 않으면 Optional.empty())
     */
    @Query("SELECT f FROM Friend f WHERE f.requester.username = :username " + "AND f.receiver.id = :receiverId AND f.status = 'PENDING'")
    Optional<Friend> findPendingRequestByUsernameAndReceiver(@Param("username") String username, @Param("receiverId") Long receiverId);

    /**
     * 친구 관계 존재 여부 확인
     * 
     * 두 사용자 사이에 수락된 친구 관계가 있는지 boolean으로 반환
     * 성능 최적화를 위해 COUNT 쿼리 사용
     * 
     * @param userId1 첫번째 사용자 ID
     * @param userId2 두번째 사용자 ID
     * @return 친구 관계 존재 여부
     */
    @Query("SELECT COUNT(f) > 0 FROM Friend f WHERE " + "(f.requester.id = :userId1 AND f.receiver.id = :userId2) OR " + "(f.requester.id = :userId2 AND f.receiver.id = :userId1) " + "AND f.status = 'ACCEPTED'")
    boolean existsFriendshipBetweenUsers(@Param("userId1") Long userId1, @Param("userId2") Long userId2);

    /**
     * 사용자의 친구 수 조회
     * 
     * 특정 사용자의 수락된 친구 관계 개수를 반환
     * 프로필 페이지에서 "친구 N명" 표시에 사용
     * 
     * @param userId 조회할 사용자 ID
     * @return 친구 수
     */
    @Query("SELECT COUNT(f) FROM Friend f WHERE " + "(f.requester.id = :userId OR f.receiver.id = :userId) " + "AND f.status = 'ACCEPTED'")
    long countFriendsByUserId(@Param("userId") Long userId);

    /**
     * 대기 중인 친구 요청 수 조회
     * 
     * 사용자가 받은 처리되지 않는 친구 요청의 개수를 반환
     * 알림 배지나 "받은 요청 N개" 표시에 사용
     * 
     * @param userId 수신자 사용자 ID
     * @return 대기 중인 친구 요청 수
     */
    @Query("SELECT COUNT(f) FROM Friend f WHERE f.receiver.id = :userId AND f.status = 'PENDING'")
    long countPendingRequestsByUserId(@Param("userId") Long userId);
}
