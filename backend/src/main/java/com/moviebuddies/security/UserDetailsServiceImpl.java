package com.moviebuddies.security;

import com.moviebuddies.entity.User;
import com.moviebuddies.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Spring Security UserDetailsService 구현체
 * 사용자명을 기반으로 데이터베이스에서 사용자 정보를 조회하고, Spring Security가 사용할 수 있는 UserDetails 객체로 변환
 * +
 * 로그인 과정에서 Spring Security가 자동으로 호출하는 서비스
 */
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * 사용자명으로 사용자 정보 조회
     * Spring Security의 인증 과정에서 호출되는 핵심 메서드
     * +
     * 로그인 시 다음 순서로 실행:
     * 1. 사용자가 로그인 요청 (username, password)
     * 2. Spring Security가 이 메서드를 호출하여 username으로 사용자 조회
     * 3. 반환된 UserDetails의 password와 입력된 password 비교
     * 4. 일치하면 인증 성공, 불일치하면 인증 실패
     *
     * @param username 로그인 시 입력된 사용자명 (the username identifying the user whose data is required.)
     * @return Spring Security가 인증에 사용할 UserDetails 객체
     * @throws UsernameNotFoundException 사용자를 찾을 수 없는 경우 발생
     */
    @Override
    @Transactional(readOnly = true) // 읽기 전용 트랜잭션으로 성능 최적화
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        // 활성 상태인 사용자만 조회 (비활성화된 계정은 로그인 불가)
        User user = userRepository.findByUsernameAndIsActiveTrue(username)
                .orElseThrow(() -> new UsernameNotFoundException(username + " 사용자를 찾을 수 없습니다"));
        
        // User 엔티티를 Spring Security가 사용할 수 있는 UserDetails로 변환
        return UserDetailsImpl.create(user);
    }
}
