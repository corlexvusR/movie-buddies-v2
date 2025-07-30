package com.moviebuddies.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 북마크 엔티티
 * 사용자가 관심 있는 영화를 북마크하는 기능을 제공
 * 사용자와 영화 간의 다대다 관계를 중간 테이블로 구현
 */
@Entity
@Table(name = "bookmarks", uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "movie_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class Bookmark {

    /**
     * 북마크 고유 식별자 (Primary Key)
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 북마크를 추가한 사용자
     * 지연 로딩으로 성능 최적화
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * 북마크된 영화
     * 지연 로딩으로 성능 최적화
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "movie_id", nullable = false)
    private Movie movie;

    /**
     * 북마크 생성 시간
     * JPA Auditing으로 자동 설정
     */
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 사용자가 영화로 북마크를 생성하는 생성자
     * 새로운 북마크 생성 시 사용
     *
     * @param user 북마크를 추가하는 사용자
     * @param movie 북마크할 영화
     */
    public Bookmark(User user, Movie movie) {
        this.user = user;
        this.movie = movie;
    }
}
