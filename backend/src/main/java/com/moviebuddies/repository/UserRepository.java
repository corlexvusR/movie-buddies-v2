package com.moviebuddies.repository;

import com.moviebuddies.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 사용자 데이터 접근 레포지토리
 * 사용자 엔티티에 대한 기본 CRUD 및 커스텀 쿼리 메서드 제공
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * 사용자명으로 사용자 조회
     * 로그인 시 사용자 인증에 사용
     * 
     * @param username 사용자명
     * @return 사용자 정보 (Optional)
     */
    Optional<User> findByUsername(String username);

    /**
     * 이메일로 사용자 조회
     * 비밀번호 찾기, 이메일 중복 확인 등에 사용
     *
     * @param email 이메일 주소
     * @return 사용자 정보 (Optional)
     */
    Optional<User> findByEmail(String email);

    /**
     * 닉네임으로 사용자 조회
     * 닉네임 중복 확인에 사용
     * 
     * @param nickname 닉네임
     * @return 사용자 정보 (Optional)
     */
    Optional<User> findByNickname(String nickname);

    /**
     * 사용자명 존재 여부 확인
     * 회원가입 시 중복 검사에 사용
     * 
     * @param username 확인할 사용자명
     * @return 존재하면 true
     */
    boolean existsByUsername(String username);

    /**
     * 이메일 존재 여부 확인
     * 회원가입 시 중복 검사에 사용
     *
     * @param email 확인할 이메일
     * @return 존재하면 true
     */
    boolean existsByEmail(String email);

    /**
     * 닉네임 존재 여부 확인
     * 프로필 수정 시 중복 검사에 사용
     * 
     * @param nickname 확인할 닉네임
     * @return 존재하면 true
     */
    boolean existsByNickname(String nickname);

    /**
     * 활성 상태인 사용자를 사용자명으로 조회
     * 로그인 시 비활성화된 계정은 제외
     * 
     * @param username 사용자명 
     * @return 활성 사용자 정보 (Optional)
     */
    Optional<User> findByUsernameAndIsActiveTrue(String username);

    /**
     * 사용자 검색 (통합 검색)
     * 닉네임, 사용자명, 이메일로 부분 일치 검색
     * 현재 사용자는 검색 결과에서 제외하고, 활성 사용자만 조회
     *
     * @param query 검색어
     * @param excludeUserId 제외할 사용자 ID (현재 사용자 )
     * @param pageable 페이징 정보
     * @return 검색된 사용자 목록 (페이징)
     */
    @Query("SELECT u FROM User u WHERE " + "LOWER(u.nickname) LIKE LOWER(CONCAT('%', :query, '%')) OR " + "LOWER(u.username) LIKE LOWER(CONCAT('%', :query, '%')) OR " + "LOWER(u.email) LIKE LOWER(CONCAT('%', :query, '%')) AND " + "u.isActive = true AND u.id != :excludeUserId")
    Page<User> searchUsers(@Param("query") String query, @Param("excludeUserId") Long excludeUserId, Pageable pageable);

    /**
     * 특정 기간 내 가입한 사용자 수 조회
     * 통계 및 관리자 대시보드에서 사용
     *
     * @param startDate 시작 날짜
     * @return 가입한 사용자 수
     */
    @Query("SELECT COUNT(u) FROM User u WHERE u.createdAt >= :startDate")
    Long countUsersCreatedAfter(@Param("startDate") java.time.LocalDateTime startDate);

    /**
     * 최근 로그인한 활성 사용자들 조회
     * 활종적인 사용자 목록 표시에 사용
     *
     * @param pageable 페이징 정보
     * @return 최근 로그인 순으로 정렬된 활성 사용자 목록
     */
    @Query("SELECT u FROM User u WHERE u.isActive = true AND u.lastLoginAt IS NOT NULL " + "ORDER BY u.lastLoginAt DESC")
    Page<User> findActiveUsersOrderByLastLogin(Pageable pageable);
}
