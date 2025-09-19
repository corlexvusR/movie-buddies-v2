package com.moviebuddies.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 영화/배우 자동완성 응답 DTO
 * 네비게이션 바 검색창에서 사용자가 키워드를 입력할 때
 * 실시간으로 관련 영화와 배우를 제안하기 위한 응답 객체
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MovieAutocompleteResponse {

    /**
     * 추천 영화 목록
     * 입력 키워드를 포함하는 영화들을 인기도 순으로 최대 5개까지 제공
     */
    private List<MovieSuggestion> movies;

    /**
     * 추천 배우 목록
     * 입력 키워드를 포함하는 배우들을 인기도 순으로 최대 5개까지 제공
     */
    private List<ActorSuggestion> actors;

    /**
     * 영화 제안 정보
     * 자동완성 드롭다운에 표시될 영화의 핵심 정보만 포함
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MovieSuggestion {

        /**
         * 영화 고유 ID
         * 사용자가 선택 시 해당 영화 상세 페이지로 이동하기 위한 식별자
         */
        private Long id;

        /**
         * 영화 제목
         * 자동완성 목록에 표시될 주요 텍스트
         */
        private String title;

        /**
         * 포스터 이미지 URL
         * 자동완성 목록에서 시각적 식별을 위한 썸네일 이미지
         */
        private String posterImageUrl;

        /**
         * 개봉 연도
         * 동명의 영화 구분을 위한 추가 정보 (nullable)
         */
        private Integer releaseYear;
    }

    /**
     * 배우 제안 정보
     * 자동완성 드롭다운에 표시될 배우의 핵심 정보만 포함
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ActorSuggestion {

        /**
         * 배우 고유 ID
         * 사용자가 선택 시 해당 배우 출연 영화 목록으로 이동하기 위한 식별자
         */
        private Long id;

        /**
         * 배우 이름
         * 자동완성 목록에 표시될 주요 텍스트
         */
        private String name;

        /**
         * 프로필 이미지 URL
         * 자동완성 목록에서 시각적 식별을 위한 프로필 사진
         */
        private String profileImageUrl;
    }
}