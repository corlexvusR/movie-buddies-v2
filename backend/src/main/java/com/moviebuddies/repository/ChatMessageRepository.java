package com.moviebuddies.repository;

import com.moviebuddies.entity.ChatMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 채팅 메시지 Repository
 * 메시지 조회 기능만 제공 (수정/삭제 기능 없음)
 */
@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    /**
     * 채팅방별 메시지 조회 (최신순)
     * 실시간 채팅 화면에서 이전 메시지 로드 용도
     * WebSocket과 함께 사용하여 초기 메시지 표시
     *
     * @param roomId 조회할 채팅방 ID
     * @param pageable 페이징 정보 (100개)
     * @return 해당 채팅방의 메시지 목록 (생성일 기준 내림차순)
     */
    @Query("SELECT cm FROM ChatMessage cm WHERE cm.chatRoom.id = :roomId ORDER BY cm.createdAt DESC")
    Page<ChatMessage> findByChatRoomIdOrderByCreatedAtDesc(@Param("roomId") Long roomId, Pageable pageable);

    /**
     * 채팅방의 최신 메시지 조회
     * 채팅방 목록에서 마지막 메시지 미리보기용
     *
     * @param roomId 조회할 채팅방 ID
     * @return 해당 채팅방의 가장 최근 메시지 (없으면 Optional.empty())
     */
    @Query("SELECT cm FROM ChatMessage cm WHERE cm.chatRoom.id = :roomId ORDER BY cm.createdAt DESC LIMIT 1")
    Page<ChatMessage> findLatestMessageByRoomId(@Param("roomId") Long roomId);

    /**
     * 특정 기간보다 오래된 메시지 조회
     * 정기적인 메시지 정리 작업용 (배치 처리에서 사용)
     *
     * @param cutoffDate 기준 날짜 (이 날짜보다 오래된 메시지 조회)
     * @return 기준 날짜보다 오래된 메시지 목록
     */
    @Query("SELECT cm FROM ChatMessage cm WHERE cm.createdAt < :cutoffDate")
    List<ChatMessage> findMessagesOlderThan(@Param("cutoffDate") LocalDateTime cutoffDate);
}
