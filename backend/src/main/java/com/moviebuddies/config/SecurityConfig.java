package com.moviebuddies.config;

import com.moviebuddies.security.JwtAuthenticationEntryPoint;
import com.moviebuddies.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

/**
 * Spring Security 설정 클래스
 * JWT 기반 인증 및 인가, CORS 정책, 보안 필터 체인을 구성합니다.
 */
@Configuration
@EnableWebSecurity  // Spring Security 활성화
@EnableMethodSecurity   // 메소드 레벨 보안 활성화 (@PreAuthorize, @PostAuthorize 등 사용 가능)
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    /**
     * 비밀번호 암호화 인코더 설정
     * BCrypt 알고리즘을 사용하여 안전한 해싱 제공
     *
     * @return BCryptPasswordEncoder 인스턴스
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * AuthenticationManager 빈 등록
     * Spring Security의 인증 처리를 담당
     * 
     * @param authConfig 인증 설정
     * @return AuthenticationManager 인스턴스
     * @throws Exception 설정 오류 시 발생
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    /**
     * Spring Security 필터 체인 설정
     * 보안 정책, 인증/인가 규칙, JWT 필터 등을 구성
     * 
     * @param http HttpSecurity 객체
     * @return 구성된 SecurityFilterChain
     * @throws Exception 설정 오류 시 발생
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // CSRF 보호 비활성화 (JWT 사용으로 불필요)
                .csrf(AbstractHttpConfigurer::disable)
                
                // CORS 설정 적용
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // 세션 관리 정책: Stateless (JWT 기반 인증)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                
                // 인증 실패 시 처리할 EntryPoint 설정
                .exceptionHandling(exception ->
                        exception.authenticationEntryPoint(jwtAuthenticationEntryPoint))
                
                // URL별 접근 권한 설정
                .authorizeHttpRequests(auth -> auth
                        // 인증 없이 접근 가능한 공개 엔드포인트
                        .requestMatchers("/auth/**").permitAll()    // 로그인, 회원가입
                        .requestMatchers("/movie/**").permitAll()   // 영화 정보 (공개)
                        .requestMatchers("/swagger-ui/**", "/api-docs/**").permitAll()  // API 문서
                        .requestMatchers("/ws/**").permitAll()  // WebSocket 연결
                        .requestMatchers("/files/**").permitAll()   // 파일 업로드/다운로드

                        // 인증이 필요한 보호된 엔드포인트
                        .requestMatchers("/users/**").authenticated()   // 사용자 관리
                        .requestMatchers("/friends/**").authenticated() // 친구 관리
                        .requestMatchers("/reviews/**").authenticated() // 리뷰 관리
                        .requestMatchers("/chat/**").authenticated()    // 채팅
                        .requestMatchers("/bookmarks/**").authenticated()   // 북마크
                        
                        // 나머지 모든 요청은 인증 필요
                        .anyRequest().authenticated()
                )
                // JWT 인증 필터를 UsernamePasswordAuthenticationFilter 이전에 추가
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * CORS(Cross-Origin Resource Sharing) 설정
     * 프론트엔드(Next.js)에서 백엔드 API 호출을 위한 CORS 정책 구성
     *
     * @return CorsConfigurationSource 설정된 CORS 정책
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration configuration = new CorsConfiguration();

        // 모든 오리진 허용 (개발 환경용, 프로덕션에서는 구체적인 도메인을 지정해야 한다)
        configuration.setAllowedOriginPatterns(Arrays.asList("*"));
        
        // 허용할 HTTP 메서드
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        
        // 허용할 헤더 (모든 헤더 허용)
        configuration.setAllowedHeaders(Arrays.asList("*"));

        // 자격 증명 (쿠키, Authorization 헤더 등) 포함 허용
        configuration.setAllowCredentials(true);
    
        // 모든 경로에 CORS 설정 적용
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
