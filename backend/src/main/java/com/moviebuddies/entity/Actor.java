package com.moviebuddies.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 배우 엔티티
 * TMDB API와 연동하여 배우 정보를 관리
 * 여러 영화에 출연할 수 있는 다대다 관계
 */
@Entity
@Table(name = "actors")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class Actor {

    /**
     * 배우 고유 식별자 (Primary Key)
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 배우 이름
     * 최대 100자까지 저장 가능
     */
    @Column(nullable = false, length = 100, columnDefinition = "TEXT")
    private String name;

    /**
     * 배우 프로필 이미지 경로
     * TMDB API에서 제공하는 프로필 이미지 경로
     */
    @Column(name = "profile_path")
    private String profilePath;

    /**
     * 배우 인기도 점수
     * TMDB에서 제공하는 인기도 지표
     */
    @Builder.Default
    private Double popularity = 0.0;

    /**
     * TMDB API의 배우 ID
     * 외부 API와의 동기화 및 중복 방지용
     */
    @Column(name = "tmdb_id", unique = true)
    private Long tmdbId;

    /**
     * 엔티티 생성 시간
     * JPA Auditing으로 자동 설정
     */
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 엔티티 마지막 수정 시간
     * JPA Auditing으로 자동 업데이트
     */
    @LastModifiedDate
    @Column(name = "last_modified_at")
    private LocalDateTime lastModifiedAt;

    /**
     * 배우가 출연한 영화들
     * Movie 엔티티의 actors 필드에 의해 매핑되는 다대다 관계
     */
    @ManyToMany(mappedBy = "actors", fetch = FetchType.LAZY)
    @Builder.Default
    private List<Movie> movies = new ArrayList<>();

    /**
     * 이름만으로 배우를 생성하는 생성자
     * 간단한 배우 정보 생성 시 사용
     * 
     * @param name 배우 이름
     */
    public Actor(String name) {
        this.name = name;
    }

    /**
     * TMDB 정보로 배우 생성하는 생성자
     * TMDB API에서 가져온 정보로 배우 생성 시 사용
     *
     * @param tmdbId TMDB 배우 ID
     * @param name 배우 이름
     * @param profilePath 프로필 이미지 경로
     * @param popularity 인기도 점수
     */
    public Actor(Long tmdbId, String name, String profilePath, Double popularity) {
        this.tmdbId = tmdbId;
        this.name = name;
        this.profilePath = profilePath;
        this.popularity = popularity;
    }

    /**
     * 프로필 이미지 URL 생성
     * TMDB 이미지 서버 URL을 조합하여 완전한 이미지 URL 반환
     *
     * @return 프로필 이미지 URL 또는 기본 이미지 URL
     */
    public String getProfileImageUrl() {
        if (profilePath != null && !profilePath.isEmpty()) {
            return profilePath.startsWith("http") ? profilePath : "https://image.tmdb.org/t/p/w185" + profilePath;
        }
        return "/api/v1/files/actor/default-profile.jpg";
    }

    /**
     * 출연 영화 개수 조회
     * 
     * @return 이 배우가 출연한 총 영화 수
     */
    public Integer getMovieCount() {
        return movies.size();
    }
}
