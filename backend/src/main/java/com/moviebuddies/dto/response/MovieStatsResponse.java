package com.moviebuddies.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 영화 통계 정보 응답 DTO
 * 전체 영화 수, 현재 상영작 수, 평균 평점 등의 통계 데이터를 전달
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MovieStatsResponse {

    /**
     * 데이터베이스에 등록된 전체 영화 수
     */
    private long totalMovies;

    /**
     * 현재 상영중인 영화 수
     */
    private long nowPlayingMovies;

    /**
     * 전체 영화의 평균 평점 (TMDB 기준)
     */
    private Double averageRating;

}
