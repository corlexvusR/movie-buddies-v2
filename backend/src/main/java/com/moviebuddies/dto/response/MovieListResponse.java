package com.moviebuddies.dto.response;

import com.moviebuddies.entity.Movie;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;


/**
 * 영화 목록 조회용 응답 DTO
 *
 * 영화 목록 페이지에서 사용되는 간소화된 영화 정보를 담는 응답 객체
 * 상세 정보 대신 목록 표시에 필요한 핵심 정보만 포함하여 네트워크 트래픽 최적화
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MovieListResponse {

    /**
     * 영화 고유 식별자 (PK)
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
     * 상영 시간 (분)
     */
    private Integer runtime;

    /**
     * 현재 상영 중 여부
     */
    private Boolean isNowPlaying;

    /**
     * 영화 장르 목록
     * 중첩된 GenreResponse 객체들의 리스트
     */
    private List<GenreResponse> genres;

    /**
     * 사용자 리뷰 총 개수
     */
    private Integer reviewCount;

    /**
     * 북마크 총 개수
     */
    private Integer bookmarkCount;

    /**
     * Movie 엔티티를 MovieListResponse DTO로 변환하는 정적 팩토리 메서드
     *
     * 엔티티의 연관 관계를 적절히 매핑하여 DTO로 변환
     * genres 컬렉션을 GenreResponse 리스트로 변환하는 과정을 포함
     *
     * @param movie 변환할 Movie 엔티티
     * @return 변환된 MovieListResponse DTO
     * @throws NullPointerException movie가 null인 경우
     */
    public static MovieListResponse from(Movie movie) {
        return MovieListResponse.builder()
                .id(movie.getId())
                .title(movie.getTitle())
                .releaseDate(movie.getReleaseDate())
                .popularity(movie.getPopularity())
                .voteCount(movie.getVoteCount())
                .voteAverage(movie.getVoteAverage())
                .overview(movie.getOverview())
                .posterImageUrl(movie.getPosterImageUrl())
                .runtime(movie.getRuntime())
                .isNowPlaying(movie.getIsNowPlaying())
                .genres(movie.getGenres().stream()
                        .map(GenreResponse::from)
                        .collect(Collectors.toList()))
                .reviewCount(movie.getReviewCount())
                .bookmarkCount(movie.getBookmarkCount())
                .build();
    }
}
