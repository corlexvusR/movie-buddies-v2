package com.moviebuddies.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 영화 엔티티
 * TMDB API와 연동하여 영화 정보를 관리
 * 장르, 배우, 북마크, 리뷰와의 관계 포함됨
 */
@Entity
@Table(name = "movies")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class Movie {

    /**
     * 영화 고유 식별자 (Primary Key)
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 영화 제목
     * 최대 200자까지 저장 가능
     */
    @Column(nullable = false, length = 200)
    private String title;

    /**
     * 영화 개봉일
     * LocalDate 타입으로 날짜만 저장
     */
    @Column(name = "release_date")
    private LocalDate releaseDate;

    /**
     * 영화 인기도 점수
     * TMDB에서 제공하는 인기도 지표
     */
    @Builder.Default
    private Double popularity = 0.0;

    /**
     * 총 투표 수
     * TMDB에서 집계된 사용자 투표 수
     */
    @Column(name = "vote_count")
    @Builder.Default
    private Integer voteCount = 0;

    /**
     * 평균 평점
     * TMDB에서 집계된 평균 평점 (0.0 - 10.0)
     */
    @Column(name = "vote_average")
    @Builder.Default
    private Double voteAverage = 0.0;

    /**
     * 영화 줄거리 또는 개요
     * TEXT 타입으로 긴 텍스트 저장 가능
     */
    @Column(columnDefinition = "TEXT")
    private String overview;

    /**
     * 포스터 이미지 경로
     * TMDB API에서 제공하는 포스터 이미지 경로
     */
    @Column(name = "poster_path")
    private String posterPath;

    /**
     * 백드롭 이미지 경로
     * TMDB API에서 제공하는 배경 이미지 경로
     */
    @Column(name = "backdrop_path")
    private String backdropPath;

    /**
     * 영화 상영 시간 (분)
     */
    @Builder.Default
    private Integer runtime = 0;

    /**
     * 현재 상영 중인지 여부
     * 영화관에서 현재 상영 중인 영화 표시용
     */
    @Column(name = "is_now_playing")
    @Builder.Default
    private Boolean isNowPlaying = false;

    /**
     * TMDB API의 영화 ID
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
     * 영화의 장르들
     * 다대다 관계로 하나의 영화가 여러 장르를 가질 수 있음
     */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "movie_genres",
            joinColumns = @JoinColumn(name = "movie_id"),
            inverseJoinColumns = @JoinColumn(name = "genre_id")
    )
    @Builder.Default
    private List<Genre> genres = new ArrayList<>();

    /**
     * 영화에 출연한 배우들
     * 다대다 관계로 하나의 영화에 여러 배우가 출연할 수 있음
     */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "movie_actors",
            joinColumns = @JoinColumn(name = "movie_id"),
            inverseJoinColumns = @JoinColumn(name = "actor_id")
    )
    @Builder.Default
    private List<Actor> actors = new ArrayList<>();

    /**
     * 이 영화를 북마크한 사용자들
     * 일대다 관계로 여러 사용자가 같은 영화를 북마크할 수 있음
     */
    @OneToMany(mappedBy = "movie", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Bookmark> bookmarks = new ArrayList<>();

    /**
     * 이 영화에 대한 리뷰들
     * 일대다 관계로 하나의 영화에 여러 리뷰가 작성될 수 있음
     */
    @OneToMany(mappedBy = "movie", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Review> reviews = new ArrayList<>();

    /**
     * 포스터 이미지 URL 생성
     * TMDB 이미지 서버 URL을 조합하여 완전한 이미지 URL 반환
     *
     * @return 포스터 이미지 URL 또는 기본 이미지 URL
     */
    public String getPosterImageUrl() {
        if (posterPath != null && !posterPath.isEmpty()) {
            return posterPath.startsWith("http") ? posterPath : "https://image.tmdb.org/t/p/w500" + posterPath;
        }
        return "/api/v1/files/movie/default-poster.jpg";
    }

    /**
     * 백드롭 이미지 URL 생성
     * TMDB 이미지 서버 URL을 조합하여 완전한 이미지 URL 반환
     * 
     * @return 백드롭 이미지 URL 또는 null (백드롭이 없는 경우)
     */
    public String getBackdropImageUrl() {
        if (backdropPath != null && !backdropPath.isEmpty()) {
            return backdropPath.startsWith("http") ? backdropPath : "https://image.tmdb.org/t/p/w780" + backdropPath;
        }
        return null;
    }

    /**
     * 사용자 리뷰 기반 평균 평점 계산
     * 시스템 내 사용자들이 작성한 리뷰의 평균 평점 계산
     *
     * @return 평균 평점 (1.0 - 5.0) 또는 0.0 (리뷰가 없는 경우)
     */
    public Double getAverageRating() {
        if (reviews.isEmpty()) {
            return 0.0;
        }
        return reviews.stream()
                .mapToDouble(Review::getRating)
                .average()
                .orElse(0.0);
    }

    /**
     * 리뷰 개수 조회
     * 
     * @return 이 영화에 작성된 총 리뷰 수
     */
    public Integer getReviewCount() {
        return reviews.size();
    }

    /**
     * 북마크 개수 조회
     * 
     * @return 이 영화를 북마크한 사용자 수
     */
    public Integer getBookmarkCount() {
        return bookmarks.size();
    }

    /**
     * 장르 추가
     * 양방향 관계를 고려하여 안전하게 장르 추가
     * 
     * @param genre 추가할 장르
     */
    public void addGenre(Genre genre) {
        if (!genres.contains(genre)) {
            genres.add(genre);
            genre.getMovies().add(this);
        }
    }

    /**
     * 장르 제거
     * 양방향 관계를 고려하여 안전하게 장르를 제거
     *
     * @param genre 제거할 장르
     */
    public void removeGenre(Genre genre) {
        genres.remove(genre);
        genre.getMovies().remove(this);
    }

    /**
     * 배우 추가
     * 양방향 관계를 고려하여 안전하게 배우를 제거
     * 
     * @param actor 추가할 배우
     */
    public void addActor(Actor actor) {
        if (!actors.contains(actor)) {
            actors.add(actor);
            actor.getMovies().add(this);
        }
    }

    /**
     * 배우 제거
     * 양방향 관계를 고려하여 안전하게 배우를 제거
     * 
     * @param actor 제거할 배우
     */
    public void removeActor(Actor actor) {
        actors.remove(actor);
        actor.getMovies().remove(this);
    }
}
