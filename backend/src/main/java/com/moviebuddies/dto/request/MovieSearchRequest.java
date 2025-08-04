package com.moviebuddies.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 영화 검색 요청 DTO
 *
 * 클라이언트에서 전송하는 영화 검색 조건들을 담는 데이터 전송 객체
 * 제목, 장르, 배우, 개봉연도, 평점, 런타임, 현재상영 등 다양한 조건으로 검색 가능
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MovieSearchRequest {

    /**
     * 검색할 영화 제목
     * 부분 일치 검색 지원 (LIKE 쿼리 사용)
     */
    private String title;

    /**
     * 필터링할 장르 ID
     * Genre 테이블의 PK 값
     */
    private Long genreId;

    /**
     * 검색할 배우 이름
     * 부분 일치 검색 지원 (LIKE 쿼리 사용)
     */
    private String actorName;

    /**
     * 필터링할 개봉 연도
     * 해당 연도에 개봉한 영화만 조회
     */
    private Integer releaseYear;

    /**
     * 최소 평점
     * 0.0 ~ 10.0 범위의 TMDB 평점
     */
    private Double minRating;

    /**
     * 최대 평점
     * 0.0 ~ 10.0 범위의 TMDB 평점
     */
    private Double maxRating;

    /**
     * 최소 런타임 (해당 값 포함하여 조회)
     */
    private Integer minRuntime;

    /**
     * 최대 런타임 (해당 값 포함하여 조회)
     */
    private Integer maxRuntime;

    /**
     * 현재 상영중 여부
     * true: 현재 상영중인 영화만, false: 상영 종료된 영화만, null: 전체
     */
    private Boolean nowPlaying;

    /**
     * 복합 검색 조건 존재 여부 확인
     *
     * 제목, 장르, 배우명 중 하나라도 검색 조건이 있는지 확인
     * 주로 JOIN을 필요로 하는 복잡한 쿼리를 생성
     *
     * @return 복합 검색 조건이 있으면 true, 없으면 false
     */
    public boolean hasComplexSearch() {
        return (title != null && !title.trim().isEmpty()) ||
                genreId != null ||
                (actorName != null && !actorName.trim().isEmpty());
    }

    /**
     * 단순 필터 조건 존재 여부 확인
     *
     * 개봉연도, 평점, 런타임, 현재상영 등 Movie 테이블 내에서만 처리 가능한 조건들 확인
     * WHERE 절만으로 처리 가능한 단순한 필터링
     *
     * @return 필터 조건이 있으면 true, 없으면 false
     */
    public boolean hasFilters() {
        return releaseYear != null ||
                minRating != null ||
                maxRating != null ||
                minRuntime != null ||
                maxRuntime != null ||
                nowPlaying != null;
    }
}
