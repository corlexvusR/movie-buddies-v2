package com.moviebuddies.security;

import com.moviebuddies.entity.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

/**
 * Spring Security UserDetails 구현체
 * User 엔티티를 Spring Security가 인식할 수 있는 형태로 래핑
 * 인증 및 인가 과정에서 사용자 정보와 권한을 제공
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDetailsImpl implements UserDetails {

    /**
     * 사용자 고유 식별자
     * JWT 토큰 생성 및 사용자 식별에 사용
     */
    private Long id;

    /**
     * 사용자명 (로그인 ID)
     * Spring Security 인증에서 사용되는 주요 식별자
     */
    private String username;

    /**
     * 암호화된 비밀번호
     * Spring Security가 인증 시 비교할 해시된 비밀번호
     */
    private String password;

    /**
     * 사용자 이메일 주소
     * 추가 사용자 정보로 활용
     */
    private String email;

    /**
     * 사용자 닉네임
     * UI에 표시될 사용자 이름
     */
    private String nickname;

    /**
     * 계정 활성화 상태
     * 비활성화된 계정은 로그인 불가
     */
    private boolean isActive;

    /**
     * 사용자 권한 목록
     * Spring Security의 역할 기반 접근 제어에 사용
     */
    private Collection<? extends GrantedAuthority> authorities;

    /**
     * User 엔티티로부터 UserDetails 구현체 생성
     * 정적 팩토리 메서드 패턴을 사용하여 객체 생성의 복잡성을 감춤
     *
     * @param user 데이터베이스에서 조회한 User 엔티티
     * @return Spring Security가 사용할 수 있는 UserDetails 구현체
     */
    public static UserDetailsImpl create(User user) {
        
        // 기본적으로 모든 사용자에게 ROLE_USER 권한 부여
        Collection<GrantedAuthority> authorities = Collections.singleton(new SimpleGrantedAuthority("ROLE_USER"));

        return new UserDetailsImpl(user.getId(), user.getUsername(), user.getPassword(), user.getEmail(), user.getNickname(), user.getIsActive(), authorities);
    }

    /**
     * 사용자 권한 목록 반환
     * Spring Security가 인가(Authorization) 과정에서 사용
     * 
     * @return 사용자가 가진 권한들의 컬렉션
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    /**
     * 사용자 비밀번호 반환
     * Spring Security가 인가(Authorization) 과정에서 사용
     *
     * @return 암호화된 비밀번호
     */
    @Override
    public String getPassword() {
        return password;
    }

    /**
     * 사용자명 반환
     * Spring Security가 사용자 식별에 사용
     *
     * @return 사용자명
     */
    @Override
    public String getUsername()  {
        return username;
    }

    /**
     * 계정 만료 여부 확인
     * 현재는 모든 계정이 만료되지 않는 것으로 설정
     * 만약에 계정 만료 정책이 필요하면 User 엔티티에 필드 추가 가능
     *
     * @return true (계정이 만료되지 않음)
     */
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    /**
     * 계정 잠금 여부 확인
     * 현재는 모든 계정이 잠기지 않는 것으로 설정
     * 만약에 보안 강화를 위해 로그인 실패 횟수 제한 등을 구현할 수 있음
     *
     * @return true (계정이 잠기지 않음)
     */
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    /**
     * 자격 증명(비밀번호) 만료 여부 확인
     * 현재는 모든 비밀번호가 만료되지 않는 것으로 설정
     * 만약에 비밀번호 주기적 변경 정책 구현이 필요하면 활용할 수 있음
     * 
     * @return true (자격 증명이 만료되지 않음)
     */
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    /**
     * 계정 활성화 여부 확인
     * User 엔티티의 isActive 필드와 연동
     * 비활성화된 계정은 로그인할 수 없음
     * 
     * @return 계정 활성화 상태
     */
    @Override
    public boolean isEnabled() {
        return isActive;
    }
}
