package com.moviebuddies.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * JPA 및 데이터베이스 관련 설정
 * 엔티티 자동 시간 추적 및 트랜잭션 관리 활성화
 */
@Configuration
@EnableJpaAuditing  // JPA Auditing 기능 활성화 
@EnableJpaRepositories(basePackages = "com.moviebuddies.repository")    // Repository 스캔 범위
@EnableTransactionManagement    // 트랜잭션 관리 활성화
public class JpaConfig {

    /**
     * JPA Auditing 자동 설정
     *
     * 활성화되는 기능:
     * - @CreatedDate: 엔티티 생성 시간 자동 설정 (Review, User, Movie 등)
     * - @LastModifiedDate: 엔티티 수정 시간 자동 업데이트
     * - @CreatedBy: 생성자 정보 자동 설정 (별도 AuditorAware 구현 시)
     * - @LastModifiedBy: 수정자 정보 자동 설정 (별도 AuditorAware 구현 시)
     */
}
