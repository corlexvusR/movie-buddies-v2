package com.moviebuddies.controller;

import com.moviebuddies.dto.request.MovieSearchRequest;
import com.moviebuddies.dto.response.ApiResponse;
import com.moviebuddies.dto.response.MovieListResponse;
import com.moviebuddies.dto.response.MovieResponse;
import com.moviebuddies.service.MovieService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 영화 관련 REST API 컨트롤러
 * 영화 조회, 검색, 필터링, 추천 등의 기능을 HTTP 엔드포인트로 제공
 */
@Slf4j
@RestController
@RequestMapping("/movies")
@RequiredArgsConstructor
@Tag(name = "Movie", description = "영화 관련 API")
public class MovieController {

    private final MovieService movieService;

    /**
     * 영화 목록 조회 (페이징 및 정렬 지원)
     * 기본 정렬: 인기도 내림차순, 페이지 크기: 20
     *
     * @param pageable 페이징 정보 (페이지 번호, 크기, 정렬)
     * @param sortBy 정렬 기준 (popularity, title, release_date, vote_average, vote_count, runtime)
     * @return 페이징된 영화 목록과 성공 메시지
     */
    @Operation(summary = "영화 목록 조회", description = "페이징된 영화 목록을 조회합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공")
    })
    @GetMapping
    public ResponseEntity<ApiResponse<Page<MovieListResponse>>> getMovies(
            @PageableDefault(size = 20, sort = "popularity", direction = Sort.Direction.DESC) Pageable pageable,
            @Parameter(description = "정렬 기준 (popularity, title, release_date, vote_average, vote_count, runtime)")
            @RequestParam(defaultValue = "popularity") String sortBy) {

        log.info("영화 목록 조회 요청 - 페이지: {}, 정렬: {}", pageable.getPageNumber(), sortBy);

        Page<MovieListResponse> movies = movieService.getMovies(pageable, sortBy);

        return ResponseEntity.ok(ApiResponse.success("영화 목록을 성공적으로 조회했습니다.", movies));
    }

    /**
     * 특정 영화의 상세 정보 조회
     * 장르, 출연진, 리뷰 통계 등 모든 정보 포함
     *
     * @param movieId 조회할 영화의 고유 ID
     * @return 영화 상세 정보와 성공 메시지
     */
    @Operation(summary = "영화 상세 정보 조회", description = "특정 영화의 상세 정보를 조회합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "영화를 찾을 수 없음")
    })
    @GetMapping("/{movieId}")
    public ResponseEntity<ApiResponse<MovieResponse>> getMovieDetail(
            @Parameter(description = "영화 ID")
            @PathVariable Long movieId) {

        log.info("영화 상세 정보 조회 요청 - ID: {}", movieId);

        MovieResponse movie = movieService.getMovieDetail(movieId);

        return ResponseEntity.ok(ApiResponse.success("영화 상세 정보를 성공적으로 조회했습니다.", movie));
    }

    /**
     * 현재 상영중인 인기 영화 TOP 5 조회
     * 인기도 기준으로 정렬된 상위 5편 반환
     *
     * @return 현재 상영중인 인기 영화 5편과 성공 메시지
     */
    @Operation(summary = "현재 상영중인 인기 영화 TOP 5", description = "현재 상영중인 영화 중 인기 순위 5위까지 조회합니다.")
    @GetMapping("/ranking/now-playing")
    public ResponseEntity<ApiResponse<List<MovieListResponse>>> getNowPlayingTop5() {

        log.info("현재 상영중인 인기 영화 TOP 5 조회 요청");

        List<MovieListResponse> movies = movieService.getNowPlayingTop5();

        return ResponseEntity.ok(ApiResponse.success("현재 상영중인 인기 영화 TOP 5를 조회했습니다.", movies));
    }

    /**
     * 특정 장르의 인기 영화 TOP 5 조회
     *
     * @param genreId 장르 고유 ID
     * @return 해당 장르의 인기 영화 5편과 성공 메시지
     */
    @Operation(summary = "장르별 인기 영화 TOP 5", description = "특정 장르의 인기 영화 5편을 조회합니다.")
    @GetMapping("/ranking/genre/{genreId}")
    public ResponseEntity<ApiResponse<List<MovieListResponse>>> getGenreTop5(
            @Parameter(description = "장르 ID")
            @PathVariable Long genreId) {

        log.info("장르별 인기 영화 TOP 5 조회 요청 - 장르 ID: {}", genreId);

        List<MovieListResponse> movies = movieService.getGenreTop5(genreId);

        return ResponseEntity.ok(ApiResponse.success("장르별 인기 영화 TOP 5를 조회했습니다.", movies));
    }

    /**
     * 복합 조건으로 영화 검색
     * 제목, 장르, 배우명을 조합한 검색 지원
     *
     * @param searchRequest 검색 조건 (제목, 장르ID, 배우명)
     * @param pageable 페이징 정보
     * @return 검색 결과와 성공 메시지
     */
    @Operation(summary = "영화 검색", description = "다양한 조건으로 영화를 검색합니다.")
    @PostMapping("/search")
    public ResponseEntity<ApiResponse<Page<MovieListResponse>>> searchMovies(
            @RequestBody @Valid MovieSearchRequest searchRequest,
            @PageableDefault(size = 20, sort = "popularity", direction = Sort.Direction.DESC) Pageable pageable) {

        log.info("영화 검색 요청 - 조건: {}", searchRequest);

        // 검색 조건 검증
        if (!searchRequest.hasAnySearchCriteria()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("최소 하나의 검색 조건을 입력해주세요."));
        }

        // 평점 범위 검증
        if (!searchRequest.isValidRatingRange()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("최소 평점은 최대 평점보다 작거나 같아야 합니다."));
        }

        // 런타임 범위 검증
        if (!searchRequest.isValidRuntimeRange()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("최소 런타임은 최대 런타임보다 작거나 같아야 합니다."));
        }

        Page<MovieListResponse> movies = movieService.searchMovies(searchRequest, pageable);
        return ResponseEntity.ok(ApiResponse.success("영화 검색을 완료했습니다.", movies));
    }

    /**
     * 특정 장르의 영화 목록 조회
     * 평점 순으로 정렬
     *
     * @param genreId 장르 고유 ID
     * @param pageable 페이징 정보
     * @return 해당 장르의 영화 목록과 성공 메시지
     */
    @Operation(summary = "장르별 영화 조회", description = "특정 장르의 영화 목록을 조회합니다.")
    @GetMapping("/genre/{genreId}")
    public ResponseEntity<ApiResponse<Page<MovieListResponse>>> getMoviesByGenre(
            @Parameter(description = "장르 ID") @PathVariable Long genreId,
            @PageableDefault(size = 20) Pageable pageable) {

        log.info("장르별 영화 조회 요청 - 장르 ID: {}", genreId);

        Page<MovieListResponse> movies = movieService.getMoviesByGenre(genreId, pageable);

        return ResponseEntity.ok(ApiResponse.success("장르별 영화 목록을 조회했습니다.", movies));
    }

    /**
     * 특정 배우가 출연한 영화 목록 조회
     *
     * @param actorId 배우 고유 ID
     * @param pageable 페이징 정보
     * @return 해당 배우가 출연한 영화 목록과 성공 메시지
     */
    @Operation(summary = "배우별 출연 영화 조회", description = "특정 배우 출연한 영화 목록을 조회합니다.")
    @GetMapping("/actor/{actorId}")
    public ResponseEntity<ApiResponse<Page<MovieListResponse>>> getMoviesByActor(
            @Parameter(description = "배우 ID") @PathVariable Long actorId,
            @PageableDefault(size = 20) Pageable pageable) {

        log.info("배우별 출연 영화 조회 요청 - 배우 ID: {}", actorId);

        Page<MovieListResponse> movies = movieService.getMoviesByActor(actorId, pageable);

        return ResponseEntity.ok(ApiResponse.success("배우별 출연 영화 목록을 조회했습니다.", movies));
    }

    /**
     * 장르 기반 영화 추천
     * 기준 영화와 유사한 장르의 다른 영화들을 추천
     *
     * @param movieId 추천 기준이 되는 영화 ID
     * @return 추천 영화 목록 (최대 10편)과 성공 메시지
     */
    @Operation(summary = "영화 추천", description = "특정 영화를 기반으로 유사한 영화를 추천합니다.")
    @GetMapping("/{movieId}/recommendations")
    public ResponseEntity<ApiResponse<List<MovieListResponse>>> getRecommendedMovies(
            @Parameter(description = "기준 영화 ID") @PathVariable Long movieId) {

        log.info("영화 추천 요청 - 기준 영화 ID: {}", movieId);

        List<MovieListResponse> movies = movieService.getRecommendedMovies(movieId);

        return ResponseEntity.ok(ApiResponse.success("추천 영화 목록을 조회했습니다.", movies));
    }

    /**
     * 특정 연도에 개봉한 영화 목록 조회
     *
     * @param year 개봉 연도
     * @param pageable 페이징 정보
     * @return 해당 연도 개봉 영화 목록과 성공 메시지
     */
    @Operation(summary = "연도별 영화 조회", description = "특정 연도에 개봉한 영화 목록을 조회합니다.")
    @GetMapping("/year/{year}")
    public ResponseEntity<ApiResponse<Page<MovieListResponse>>> getMoviesByYear(
            @Parameter(description = "개봉 연도") @PathVariable Integer year,
            @PageableDefault(size = 20) Pageable pageable) {

        log.info("연도별 영화 조회 요청 - 연도: {}", year);

        Page<MovieListResponse> movies = movieService.getMoviesByYear(year, pageable);

        return ResponseEntity.ok(ApiResponse.success(year + "년 영화 목록을 조회했습니다.", movies));
    }

    /**
     * 평점 범위로 영화 필터링
     * TMDB 평점 기준으로 범위 검색
     *
     * @param minRating 최소 평점
     * @param maxRating 최대 평점
     * @param pageable 페이징 정보
     * @return 해당 평점 범위의 영화 목록과 성공 메시지
     */
    @Operation(summary = "평점 범위로 영화 조회", description = "지정된 평점 범위의 영화 목록을 조회합니다.")
    @GetMapping("/rating")
    public ResponseEntity<ApiResponse<Page<MovieListResponse>>> getMoviesByRating(
            @Parameter(description = "최소 평점") @RequestParam Double minRating,
            @Parameter(description = "최대 평점") @RequestParam Double maxRating,
            @PageableDefault(size = 20) Pageable pageable) {

        log.info("평점 범위 영화 조회 요청 - 범위: {} ~ {}", minRating, maxRating);

        Page<MovieListResponse> movies = movieService.getMoviesByRating(minRating, maxRating, pageable);

        return ResponseEntity.ok(ApiResponse.success("평점 범위 영화 목록을 조회했습니다.", movies));
    }

    /**
     * 런타임(상영시간) 범위로 영화 필터링
     *
     * @param minRuntime 최소 런타임 (분)
     * @param maxRuntime 최대 런타임 (분)
     * @param pageable 페이징 정보
     * @return 해당 런타임 범위의 영화 목록과 성공 메시지
     */
    @Operation(summary = "런타임 범위로 영화 조회", description = "지정된 런타임 범위의 영화 목록을 조회합니다.")
    @GetMapping("/runtime")
    public ResponseEntity<ApiResponse<Page<MovieListResponse>>> getMoviesByRuntime(
            @Parameter(description = "최소 런타임(분)") @RequestParam Integer minRuntime,
            @Parameter(description = "최대 런타임(분)") @RequestParam Integer maxRuntime,
            @PageableDefault(size = 20) Pageable pageable) {

        log.info("런타임 범위 영화 조회 요청 - 범위: {}분 ~ {}분", minRuntime, maxRuntime);

        Page<MovieListResponse> movies = movieService.getMoviesByRuntime(minRuntime, maxRuntime, pageable);

        return ResponseEntity.ok(ApiResponse.success("런타임 범위 영화 목록을 조회했습니다.", movies));
    }
}