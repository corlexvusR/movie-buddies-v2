package com.moviebuddies.repository;

import com.moviebuddies.entity.ChatRoom;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 채팅방 Repository
 * 공개 채팅방의 기본적인 CRUD 및 조회 기능 제공
 */
@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

    /**
     * 활성 채팅방 목록 조회 (최신순)
     * 메인 채팅방 목록 페이지에서 사용
     *
     * @param pageable 페이징 정보 (크기, 정렬 등)
     * @return 활성화된 채팅방 목록 (생성일 기준 내림차순)
     */
    Page<ChatRoom> findByIsActiveTrueOrderByCreatedAtDesc(Pageable pageable);

    /**
     * 사용자가 참가한 활성 채팅방 목록
     * 사용자의 개인 채팅방 목록 페이지에서 사용
     *
     * @param userId 조회할 사용자 ID
     * @return 해당 사용자가 참가한 활성 채팅방 목록
     */
    @Query("SELECT DISTINCT cr FROM ChatRoom cr JOIN cr.participants p WHERE p.id = :userId AND cr.isActive = true")
    List<ChatRoom> findByParticipantsIdAndIsActiveTrue(@Param("userId") Long userId);

    /**
     * 채팅방 이름 중복 확인
     * 새 채팅방 생성 시 이름 유효성 검사용
     *
     * @param name 확인할 채팅방 이름
     * @return 해당 이름의 채팅방이 존재하면 true
     */
    boolean existsByName(String name);

    /**
     * 이름으로 채팅방 조회
     * 특정 채팅방 검색 기능에서 사용
     *
     * @param name 조회할 채팅방 이름
     * @return 해당 이름의 채팅방 (존재하지 않으면 Optional.empty())
     */
    Optional<ChatRoom> findByName(String name);
}
