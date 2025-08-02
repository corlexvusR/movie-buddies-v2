package com.moviebuddies;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Movie Buddies 백엔드 애플리케이션 메인 클래스
 *
 * Spring Boot 애플리케이션의 진입점이며, 필요한 기능들을 활성화
 * - JPA Auditing: 엔티티의 생성/수정 시간 자동 관리
 * - 캐싱: Redis 기반 캐시 시스템 활성화
 * - 비동기 처리: 파일 업로드, 이메일 발송 등 비동기 작업 지원
 * - 스케줄링: 정기적인 배치 작업 및 클린업 작업 지원
 */
@SpringBootApplication	// Spring Boot 자동 설정 및 컴포넌트 스캔 활성화
@EnableJpaAuditing	// JPA Auditing 기능 활성화 (createdAt, lastModifiedAt 자동 관리)
@EnableCaching	// Spring Cache 추상화 활성화 (Redis 캐시 사용)
@EnableAsync	// 비동기 메서드 실행 지원 (@Async 어노테이션 사용 가능)
@EnableScheduling	// 스케줄링 기능 활성화 (@Scheduled 어노테이션 사용 가능)
public class MovieBuddiesBackendApplication {

	/**
	 * 애플리케이션 메인 메서드
	 * Spring Boot 애플리케이션을 시작하는 진입점
	 * 
	 * @param args 명령행 인수
	 */
	public static void main(String[] args) {
		SpringApplication.run(MovieBuddiesBackendApplication.class, args);
	}

}
