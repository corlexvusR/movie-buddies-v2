package com.moviebuddies.dto.response;

import com.moviebuddies.entity.Movie;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MovieResponse {

    /**
     * 영화 ID
     */
    private Long id;

    /**
     * 영화 제목
     */
    private String title;

    /**
     * 개봉일
     */
    private LocalDate releaseDate;

    /**
     * TMDB 인기도 점수
     */
    private Double popularity;

    /**
     * TMDB 투표 수
     */
    private Integer voteCount;

    /**
     * TMDB 평균 평점
     */
    private Double voteAverage;

    /**
     * 영화 줄거리
     */
    private String overview;

    /**
     * 포스터 이미지 URL (TMDB 이미지 서버 기반)
     */
    private String posterImageUrl;

    /**
     * 배경 이미지 URL (TMDB 이미지 서버 기반)
     */
    private String backdropImageUrl;

    /**
     * 상영 시간 (분)
     */
    private Integer runtime;

    /**
     * 현재 상영 중 여부
     */
    private Boolean isNowPlaying;

    /**
     * TMDB API의 영화 ID
     */
    private Long tmdbId;

    /**
     * 데이터베이스 등록 시간
     */
    private LocalDateTime createdAt;

    /**
     * 영화 장르 목록
     */
    private List<GenreResponse> genres;

    /**
     * 주요 출연지 목록
     */
    private List<ActorResponse> actors;

    /**
     * 사용자 리뷰 평균 평점
     */
    private Double averageRating;

    /**
     * 사용자 리뷰 총 개수
     */
    private Integer reviewCount;

    /**
     * 북마크 총 개수
     */
    private Integer bookmarkCount;

    /**
     * Movie 엔티티를 MovieResponse DTO로 변환
     * 연관된 장르, 배우 정보도 함께 변환하여 포함
     *
     * @param movie 변환할 Movie 엔티티
     * @return MovieResponse DTO 객체
     */
    public static MovieResponse from(Movie movie) {
        return MovieResponse.builder()
                .id(movie.getId())
                .title(movie.getTitle())
                .releaseDate(movie.getReleaseDate())
                .popularity(movie.getPopularity())
                .voteCount(movie.getVoteCount())
                .voteAverage(movie.getVoteAverage())
                .overview(movie.getOverview())
                .posterImageUrl(movie.getPosterImageUrl())
                .backdropImageUrl(movie.getBackdropImageUrl())
                .runtime(movie.getRuntime())
                .isNowPlaying(movie.getIsNowPlaying())
                .tmdbId(movie.getTmdbId())
                .createdAt(movie.getCreatedAt())
                .genres(movie.getGenres().stream()
                        .map(GenreResponse::from)
                        .collect(Collectors.toList()))
                .actors(movie.getActors().stream()
                        .map(ActorResponse::from)
                        .collect(Collectors.toList()))
                .averageRating(movie.getAverageRating())
                .reviewCount(movie.getReviewCount())
                .bookmarkCount(movie.getBookmarkCount())
                .build();
    }
}
