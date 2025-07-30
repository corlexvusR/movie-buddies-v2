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
 * 장르 엔티티
 * 영화의 장르 정보를 관리하며 TMDB API와 연동
 * 여러 영화가 같은 장르를 가질 수 있는 다대다 관계
 */
@Entity
@Table(name = "genres")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class Genre {

    /**
     * 장르 고유 식별자 (Primary Key)
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 장르 이름
     * 고유값이며 최대 50자까지 저장 가능
     */
    @Column(nullable = false, unique = true, length = 50)
    private String name;

    /**
     * TMDB API의 장르 ID
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
     * 이 장르에 속하는 영화들
     * Movie 엔티티의 genres 필드에 의해 매핑되는 다대다 관계
     */
    @ManyToMany(mappedBy = "genres", fetch = FetchType.LAZY)
    @Builder.Default
    private List<Movie> movies = new ArrayList<>();

    /**
     * 이름만으로 장르 생성하는 생성자
     * 간단한 장르 정보 생성 시 사용
     * 
     * @param name 장르 이름
     */
    public Genre(String name) {
        this.name = name;
    }

    /**
     * TMDB 정보로 장르 생성하는 생성자
     * TMDB API에서 가져온 정보로 장르 생성 시 사용
     *
     * @param tmdbId TMDB 장르 ID
     * @param name 장르 이름
     */
    public Genre(Long tmdbId, String name) {
        this.tmdbId = tmdbId;
        this.name = name;
    }

    /**
     * 이 장르에 속하는 영화 개수 조회
     * 
     * @return 이 장르에 속하는 총 영화 수
     */
    public Integer getMovieCount() {
        return movies.size();
    }
}
