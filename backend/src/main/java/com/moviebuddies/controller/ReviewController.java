package com.moviebuddies.controller;

import com.moviebuddies.dto.request.ReviewRequest;
import com.moviebuddies.dto.response.MovieRatingDistributionResponse;
import com.moviebuddies.dto.response.ReviewResponse;
import com.moviebuddies.dto.response.ApiResponse;
import com.moviebuddies.security.UserDetailsImpl;
import com.moviebuddies.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * 리뷰 REST API 컨트롤러
 * 영화 리뷰의 CRUD 및 조회 기능 제공
 */
@Slf4j
@RestController
@RequestMapping("/reviews")
@RequiredArgsConstructor
@Tag(name = "Review", description = "리뷰 관련 API")
public class ReviewController {

    private final ReviewService reviewService;

    /**
     * 특정 영화의 모든 리뷰 조회 (영화 상세페이지용)
     *
     * @param movieId 영화 ID
     * @param pageable 페이징 정보 (10개씩, 최신순)
     * @return 영화의 리뷰 목록 (페이징)
     */
    @Operation(summary = "영화별 리뷰 조회", description = "특정 영화의 모든 리뷰를 조회합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "영화를 찾을 수 없음")
    })
    @GetMapping("/movie/{movieId}")
    public ResponseEntity<ApiResponse<Page<ReviewResponse>>> getReviewsByMovie(
            @Parameter(description = "영화 ID") @PathVariable Long movieId,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        log.info("영화별 리뷰 조회 요청 - 영화 ID: {}", movieId);

        Page<ReviewResponse> reviews = reviewService.getReviewsByMovie(movieId, pageable);

        return ResponseEntity.ok(ApiResponse.success("영화 리뷰 목록을 조회했습니다.", reviews));
    }

    /**
     * 특정 사용자의 모든 리뷰 조회 (마이페이지용)
     * 
     * @param username 사용자명
     * @param pageable 페이징 정보 (10개씩, 최신순)
     * @return 사용자의 리뷰 목록 (페이징)
     */
    @Operation(summary = "사용자별 리뷰 조회", description = "특정 사용자가 작성한 모든 리뷰를 조회합니다.")
    @GetMapping("/user/{username}")
    public ResponseEntity<ApiResponse<Page<ReviewResponse>>> getReviewsByUser(
            @Parameter(description = "사용자명") @PathVariable String username,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        log.info("사용자별 리뷰 조회 요청 - 사용자: {}", username);

        Page<ReviewResponse> reviews = reviewService.getReviewsByUser(username, pageable);

        return ResponseEntity.ok(ApiResponse.success("사용자 리뷰 목록을 조회했습니다.", reviews));
    }

    /**
     * 새 리뷰 작성
     *
     * @param request 리뷰 작성 요청 데이터 (영화 ID, 내용, 평점)
     * @param userDetails 인증된 사용자 정보
     * @return 작성된 리뷰 정보
     */
    @Operation(summary = "리뷰 작성", description = "특정 영화의 새로운 리뷰를 작성합니다.")
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "리뷰 작성 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "영화를 찾을 수 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "중복 리뷰")
    })
    @PostMapping
    public ResponseEntity<ApiResponse<ReviewResponse>> createReview(
            @Valid @RequestBody ReviewRequest request,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        log.info("리뷰 작성 요청 - 사용자 ID: {}, 영화 ID: {}", userDetails.getId(), request);

        ReviewResponse review = reviewService.createReview(userDetails.getId(), request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("리뷰가 성공적으로 작성되었습니다.", review));
    }

    /**
     * 기존의 영화 리뷰 수정
     *
     * @param movieId 영화 ID
     * @param request 리뷰 수정 요청 데이터
     * @param userDetails 인증된 사용자 정보
     * @return 수정된 리뷰 정보
     */
    @Operation(summary = "리뷰 수정", description = "기존의 영화 리뷰를 수정합니다.")
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "리뷰 수정 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "권한 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "리뷰를 찾을 수 없음")
    })
    @PutMapping("/movie/{movieId}")
    public ResponseEntity<ApiResponse<ReviewResponse>> updateReview(
            @Parameter(description = "영화 ID") @PathVariable Long movieId,
            @Valid @RequestBody ReviewRequest request,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        log.info("리뷰 수정 요청 - 사용자 ID: {}, 영화 ID: {}", userDetails.getId(), movieId);

        ReviewResponse review = reviewService.updateReview(userDetails.getId(), movieId, request);

        return ResponseEntity.ok(ApiResponse.success("리뷰가 성공적으로 수정되었습니다.", review));
    }

    /**
     * 리뷰 삭제 (리뷰 작성자만 가능)
     *
     * @param movieId 영화 ID
     * @param reviewId 리뷰 ID
     * @param userDetails 인증된 사용자 정보
     * @return 삭제 완료 응답
     */
    @Operation(summary = "리뷰 삭제", description = "기존 리뷰를 삭제합니다.")
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "리뷰 삭제 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "권한 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "리뷰를 찾을 수 없음")
    })
    @DeleteMapping("/movie/{movieId}/{reviewId}")
    public ResponseEntity<ApiResponse<Void>> deleteReview(
            @Parameter(description = "영화 ID") @PathVariable Long movieId,
            @Parameter(description = "리뷰 ID") @PathVariable Long reviewId,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        log.info("리뷰 삭제 요청 - 사용자 ID: {}, 영화 ID: {}, 리뷰 ID: {}", userDetails.getId(), movieId, reviewId);

        reviewService.deleteReview(userDetails.getId(), movieId, reviewId);

        return ResponseEntity.ok(ApiResponse.success("리뷰가 성공적으로 삭제되었습니다."));
    }

    /**
     * 로그인한 사용자의 특정 영화 리뷰 조회 (영화 상세페이지에서 사용자가 리뷰를 작성했었다면 해당 내용을 리뷰 섹션 위에 출력)
     *
     * @param movieId 영화 ID
     * @param userDetails 인증된 사용자 정보
     * @return 사용자의 해당 영화 리뷰 (없으면 null)
     */
    @Operation(summary = "사용자의 특정 영화 리뷰 조회", description = "로그인한 사용자의 특정 영화에 대한 리뷰를 조회합니다.")
    @SecurityRequirement(name = "Bearer Authentication")
    @GetMapping("/my/movie/{movieId}")
    public ResponseEntity<ApiResponse<ReviewResponse>> getMyReviewForMovie(
            @Parameter(description = "영화 ID") @PathVariable Long movieId,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        log.info("사용자 영화 리뷰 조회 요청 - 사용자 ID: {}, 영화 ID: {}", userDetails.getId(), movieId);

        try {
            ReviewResponse review = reviewService.getUserReviewForMovie(userDetails.getId(), movieId);
            return ResponseEntity.ok(ApiResponse.success("사용자의 영화 리뷰를 조회했습니다.", review));
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.success("작성된 리뷰가 없습니다.", null));
        }
    }

    /**
     * 영화별 평점 분포 조회 (영화 상세페이지에서 현재 영화에 대한 평점 분포를 시각화)
     *
     * @param movieId 영화 ID
     * @return 평점별 리뷰 수 분포 데이터
     */
    @Operation(summary = "영화 평점 분포", description = "특정 영화의 평점 분포를 조회합니다.")
    @GetMapping("/movie/{movieId}/rating-distribution")
    public ResponseEntity<ApiResponse<MovieRatingDistributionResponse>> getMovieRatingDistribution(
            @Parameter(description = "영화 ID") @PathVariable Long movieId) {

        log.info("영화 평점 분포 조회 요청 - 영화 ID: {}", movieId);

        MovieRatingDistributionResponse distribution = reviewService.getMovieRatingDistribution(movieId);

        return ResponseEntity.ok(ApiResponse.success("영화 평점 분포를 조회했습니다.", distribution));
    }
}
