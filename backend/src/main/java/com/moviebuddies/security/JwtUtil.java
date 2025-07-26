package com.moviebuddies.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * JWT(JSON Web Token) 유틸리티 클래스
 * +
 * 주요 기능:
 * - JWT 액세스 토큰 생성 및 검증
 * - JWT 리프레시 토큰 생성 및 검증
 * - 토큰에서 사용자 정보 추출
 * - 토큰 만료 여부 확인
 * - HMAC-SHA 알고리즘을 이용한 서명/검증
 */

@Slf4j
@Component
public class JwtUtil {

    // application.yml에서 주입받는 JWT 시크릿 키
    @Value("${jwt.secret}")
    private String secret;

    // 액세스 토큰 만료 시간 (밀리초, 기본값: 24시간)
    @Value("${jwt.expiration}")
    private Long expiration;

    // 리프레시 토큰 만료 시간 (밀리초, 기본값: 7일)
    @Value("${jwt.refresh-expiration}")
    private Long refreshExpiration;

    /**
     * JWT 서명용 SecretKey 생성
     * HMAC-SHA 알고리즘을 사용하여 시크릿 키를 바이트 배열로 변환
     *
     * @return SecretKey JWT 서명/검증용 키
     */
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    /**
     * JWT 토큰에서 사용자명(username) 추출
     * 
     * @param token JWT 토큰 문자열
     * @return string 사용자명
     */
    public String getUsernameFromToken(String token) {
        return getClaimFromToken(token, Claims::getSubject);
    }

    /**
     * JWT 토큰에서 만료 시간 추출
     * 
     * @param token JWT 토큰 문자열
     * @return Date 토큰 만료 시간
     */
    public Date getExpirationDateFromToken(String token) {
        return getClaimFromToken(token, Claims::getExpiration);
    }

    /**
     * JWT 토큰에서 특정 클레임(Claims) 추출
     * 
     * @param token JWT 토큰 문자열
     * @param claimsResolver 클레임 추출 함수
     * @param <T> 반환할 클레임의 타입
     * @return T 추출된 클레임 값
     */
    public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }

    /**
     * JWT 토큰에서 모든 클레임(Claims) 추출
     * 
     * @param token JWT 토큰 문자열
     * @return Claims 토큰의 모든 클레임 정보
     * @throws JwtException 토큰이 유효하지 않거나 서명 검증 실패시
     */
    private Claims getAllClaimsFromToken(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())    // 서명 검증
                .build()
                .parseSignedClaims(token)   //토큰 파싱 및 검증
                .getPayload();  // 클레임 정보 반환
    }

    /**
     * JWT 토큰 만료 여부 확인
     *
     * @param token JWT 토큰 문자열
     * @return Boolean true: 만료됨, false: 유효함
     */
    public Boolean isTokenExpired(String token) {
        try {
            final Date expiration = getExpirationDateFromToken(token);
            return expiration.before(new Date());
        } catch (JwtException e) {
            log.error("JWT token is expired: {}", e.getMessage());
            return true;
        }
    }

    /**
     * 사용자 정보를 기반으로 JWT 액세스 토큰 생성
     *
     * @param userDetails Spring Security UserDetails 객체
     * @return String 생성된 JWT 액세스 토큰
     */
    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        return createToken(claims, userDetails.getUsername(), expiration);
    }

    /**
     * 사용자 정보를 기반으로 JWT 리프레시 토큰 생성
     * 리프레시 토큰은 액세스 토큰보다 긴 만료 시간을 가짐
     *
     * @param userDetails Spring Security UserDetails 객체
     * @return String 생성된 JWT 리프레시 토큰
     */
    public String generateRefreshToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("type", "refresh");
        return createToken(claims, userDetails.getUsername(), refreshExpiration);
    }

    /**
     * JWT 토큰 생성 내부 메서드
     * 
     * @param claims 토큰에 포함할 추가 클레임 정보
     * @param subject 토큰 주체 (일반적으로는 사용자명)
     * @param expiration 토큰 만료 시간 (밀리초)
     * @return String 생성된 JWT 토큰
     */
    private String createToken(Map<String, Object> claims, String subject, Long expiration) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .claims(claims) // 커스텀 클레임 추가
                .subject(subject)   // 토큰 주체 (사용자명)
                .issuedAt(now)  // 토큰 발급 시간
                .expiration(expiryDate) // 토큰 만료 시간
                .signWith(getSigningKey())  // 서명 알고리즘 및 키 설정
                .compact(); // 토큰 문자열로 변환
    }

    /**
     * JWT 토큰과 사용자 정보를 비교하여 토큰 유효성 검증
     *
     * @param token JWT 토큰 문자열
     * @param userDetails 검증할 사용자 정보
     * @return Boolean true: 유효한 토큰, false: 유효하지 않은 토큰
     */
    public Boolean validateToken(String token, UserDetails userDetails) {
        try {
            final String username = getUsernameFromToken(token);
            return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
        } catch (JwtException | IllegalArgumentException e) {
            log.error("JWT token validation failed(userDetails): {}", e.getMessage());
            return false;
        }
    }

    /**
     * JWT 토큰의 기본적인 유효성 검증 (서명 및 만료 시간 확인)
     *
     * @param token JWT 토큰 문자열
     * @return Boolean true: 유효한 토큰, false: 유효하지 않은 토큰
     */
    public Boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.error("JWT token validation failed: {}", e.getMessage());
            return false;

        }
    }
}
