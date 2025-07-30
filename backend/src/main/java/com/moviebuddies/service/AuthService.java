package com.moviebuddies.service;

import com.moviebuddies.dto.request.LoginRequest;
import com.moviebuddies.dto.request.SignupRequest;
import com.moviebuddies.dto.response.JwtResponse;
import com.moviebuddies.dto.response.UserResponse;
import com.moviebuddies.entity.User;
import com.moviebuddies.exception.BusinessException;
import com.moviebuddies.repository.UserRepository;
import com.moviebuddies.security.JwtUtil;
import com.moviebuddies.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 인증 및 사용자 관리 서비스
 *
 * 사용자 회원가입, 로그인, 토큰 갱신 등의 인증 관련 비즈니스 로직을 처리
 * JWT 기반 인증 시스템을 통해 보안성과 확장성을 제공
 *
 * 주요 기능:
 * - 회원가입 및 중복 검증
 * - 로그인 및 JWT 토큰 발급
 * - 리프레시 토큰을 통한 액세스 토큰 갱신
 * - 사용자명/이메일/닉네임 중복 확인
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true) // 기본적으로 읽기 전용 트랜잭션 사용
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    /**
     * 사용자 회원가입 처리
     *
     * 입력된 사용자 정보의 중복 여부를 검사한 후 새로운 사용자를 생성
     * 비밀번호를 BCrypt로 안전하게 암호화되어 저장
     *
     * @param request 회원가입 요청 정보 (사용자명, 비밀번호, 이메일, 닉네임)
     * @return 생성된 사용자 정보 DTO
     * @throws BusinessException 중복된 사용자명/이메일/닉네임이 존재하는 경우
     */
    @Transactional  // 쓰기 작업이므로 트랜잭션 적용
    public UserResponse signup(SignupRequest request) {

        log.info("회원가입 요청: {}", request.getUsername());
        
        // 중복 검사 수행 (사용자명, 이메일, 닉네임)
        validateSignupRequest(request);
        
        // 새로운 사용자 엔티티 생성
        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .email(request.getEmail())
                .nickname(request.getNickname())
                .isActive(true)
                .build();

        User savedUser = userRepository.save(user);
        log.info("회원가입 완료: {}", savedUser.getUsername());

        return UserResponse.from(savedUser);
    }

    /**
     * 사용자 로그인 처리
     *
     * Spring Security의 AuthenticationManager를 통해 사용자 인증을 수행하고, 성공 시 JWT 액세스 토큰과 리프레시 토큰을 발급
     * 마지막 로그인 시간도 함께 업데이트
     *
     * @param request 로그인 요청 정보 (사용자명, 비밀번호)
     * @return JWT 토큰과 사용자 정보를 포함한 응답 DTO
     * @throws BusinessException 인증 실패 또는 사용자를 찾을 수 없는 경우
     */
    @Transactional  // 마지막 로그인 시간 업데이트를 위한 트랜잭션
    public JwtResponse login(LoginRequest request) {

        log.info("로그인 요청: {}", request.getUsername());

        // Spring Security를 통한 사용자 인증 처리
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        // 인증 성공 시 Security Context에 인증 정보 설정
        SecurityContextHolder.getContext().setAuthentication(authentication);
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        // JWT 액세스 토큰과 리프레시 토큰 생성
        String accessToken = jwtUtil.generateToken(userDetails);
        String refreshToken = jwtUtil.generateRefreshToken(userDetails);

        // 마지막 로그인 시간 업데이트
        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> BusinessException.notFound("사용자를 찾을 수 없습니다."));
        user.updateLastLoginAt();

        log.info("로그인 성공: {}", request.getUsername());

        return JwtResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .user(UserResponse.from(user))
                .build();
    }

    /**
     * 리프레시 토큰을 사용한 액세스 토큰 갱신
     *
     * 만료된 액세스 토큰을 새로 발급받기 위해 리프레시 토큰을 검증하고, 유효한 경우 새로운 액세스 토큰을 생성하여 반환
     *
     * @param refreshToken 클라이언트에서 전송한 리프레시 토큰
     * @return 새로운 액세스 토큰과 사용자 정보를 포함한 응답 DTO
     * @throws BusinessException 리프레시 토큰이 유효하지 않거나 사용자를 찾을 수 없는 경우
     */
    public JwtResponse refreshToken(String refreshToken) {

        log.info("토큰 새로고침 요청");

        // 리프레시 토큰 유효성 검증
        if (!jwtUtil.validateToken(refreshToken)) {
            throw BusinessException.unauthorized("유효하지 않은 리프레시 토큰입니다.");
        }

        // 리프레시 토큰에서 사용자명 추출
        String username = jwtUtil.getUsernameFromToken(refreshToken);
        User user = userRepository.findByUsernameAndIsActiveTrue(username)
                .orElseThrow(() -> BusinessException.notFound("사용자를 찾을 수 없습니다."));

        // 새로운 액세스 토큰 생성
        UserDetailsImpl userDetails = UserDetailsImpl.create(user);
        String newAccessToken = jwtUtil.generateToken(userDetails);
    
        return JwtResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(refreshToken) // 기존 리프레시 토큰 재사용
                .tokenType("Bearer")
                .user(UserResponse.from(user))
                .build();
    }

    /**
     * 회원가입 요청 유효성 검사
     *
     * 사용자명, 이메일, 닉네임의 중복 여부를 확인하여 중복된 정보가 있으면 예외를 발생
     * 
     * @param request 회원가입 요청 정보
     * @throws BusinessException 중복된 정보가 존재하는 경우
     */
    private void validateSignupRequest(SignupRequest request) {

        if (userRepository.existsByUsername(request.getUsername())) {
            throw BusinessException.conflict("이미 사용 중인 사용자명입니다.");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw BusinessException.conflict("이미 사용 중인 이메일입니다.");
        }

        if (userRepository.existsByNickname(request.getNickname())) {
            throw BusinessException.conflict("이미 사용 중인 닉네임입니다.");
        }
    }

    /**
     * 사용자명 사용 가능 여부 확인
     * 
     * @param username 확인할 사용자명
     * @return 사용 가능하면 true, 이미 사용 중이면 false
     */
    public boolean isUsernameAvailable(String username) {
        return !userRepository.existsByUsername(username);
    }

    /**
     * 이메일 사용 가능 여부 확인
     * 
     * @param email 확인할 이메일
     * @return 사용 가능하면 true, 이미 사용 중이면 false
     */
    public boolean isEmailAvailable(String email) {
        return !userRepository.existsByEmail(email);
    }

    /**
     * 닉네임 사용 가능 여부 확인
     * 
     * @param nickname 확인할 닉네임
     * @return 사용 가능하면 true, 이미 사용 중이면 false
     */
    public boolean isNicknameAvailable(String nickname) {
        return !userRepository.existsByNickname(nickname);
    }
}
