package com.moviebuddies.controller;

import com.moviebuddies.dto.response.ApiResponse;
import com.moviebuddies.dto.response.BookmarkResponse;
import com.moviebuddies.dto.response.BookmarkStatsResponse;
import com.moviebuddies.dto.response.MovieBookmarkStatsResponse;
import com.moviebuddies.security.UserDetailsImpl;
import com.moviebuddies.service.BookmarkService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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

import java.util.List;

/**
 * 북마크 관련 REST API 컨트롤러
 * 사용자의 영화 북마크 추가/삭제, 조회, 통계 기능을 HTTP 엔드포인트로 제공
 * JWT 인증이 필요한 개인화 기능과 공개 조회 기능을 모두 포함
 */
@Slf4j
@RestController
@RequestMapping("/bookmarks")
@RequiredArgsConstructor
@Tag(name = "bookmark", description = "북마크 관련 API")
public class BookmarkController {

    // 의존성 주입을 위해 final 사용
    private final BookmarkService bookmarkService;

    /**
     * 새로운 북마크 추가
     * 로그인한 사용자가 특정 영화를 북마크에 추가
     *
     * @param movieId 북마크할 영화 ID
     * @param userDetails 인증된 사용자 정보
     * @return 생성된 북마크 정보와 성공 메시지
     */
    @Operation(summary = "북마크 추가", description = "영화를 북마크에 추가합니다.")
    @SecurityRequirement(name = "Bearer Authentication")
    @PostMapping("/movies/{movieId}")
    public ResponseEntity<ApiResponse<BookmarkResponse>> addBookmark(
            @Parameter(description = "영화 ID") @PathVariable Long movieId,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        log.info("북마크 추가 요청 - 사용자 ID: {}, 영화 ID: {}", userDetails.getId(), movieId);

        BookmarkResponse bookmark = bookmarkService.addBookmark(userDetails.getId(), movieId);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("북마크가 추가되었습니다.", bookmark));
    }

    /**
     * 기존 북마크 삭제
     * 로그인한 사용자의 북마크에서 특정 영화를 제거
     *
     * @param movieId 삭제할 영화 ID
     * @param userDetails 인증된 사용자 정보
     * @return 성공 메시지
     */
    @Operation(summary = "북마크 삭제", description = "영화를 북마크에서 삭제합니다.")
    @SecurityRequirement(name = "Bearer Authentication")
    @DeleteMapping("/movies/{movieId}")
    public ResponseEntity<ApiResponse<Void>> removeBookmark(
            @Parameter(description = "영화 ID") @PathVariable Long movieId,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        log.info("북마크 삭제 요청 - 사용자 ID: {}, 영화 ID: {}", userDetails.getId(), movieId);

        bookmarkService.removeBookmark(userDetails.getId(), movieId);

        return ResponseEntity.ok(ApiResponse.success("북마크가 삭제되었습니다."));
    }

    /**
     * 로그인한 사용자의 북마크 목록 조회
     * 개인의 북마크 관리 페이지에서 사용
     *
     * @param pageable 페이징 정보 (기본: 20개, 생성일 내림차순)
     * @param userDetails 인증된 사용자 정보
     * @return 페이징된 북마크 목록
     */
    @Operation(summary = "내 북마크 목록", description = "로그인한 사용자의 북마크 목록을 조회합니다.")
    @SecurityRequirement(name = "Bearer Authentication")
    @GetMapping("/my")
    public ResponseEntity<ApiResponse<Page<BookmarkResponse>>> getMyBookmarks(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        log.info("내 북마크 목록 조회 요청 - 사용자 ID: {}", userDetails.getId());

        Page<BookmarkResponse> bookmarks = bookmarkService.getUserBookmarks(userDetails.getUsername(), pageable);

        return ResponseEntity.ok(ApiResponse.success("북마크 목록입니다.", bookmarks));
    }

    /**
     * 특정 사용자의 북마크 목록 조회 (목록 공개됨)
     * 다른 사용자의 프로필 확인에 사용
     *
     * @param username 조회할 사용자명
     * @param pageable 페이징 정보
     * @return 해당 사용자의 페이징된 북마크 목록
     */
    @Operation(summary = "사용자 북마크 목록", description = "특정 사용자의 북마크 목록을 조회합니다.")
    @GetMapping("/user/{username}")
    public ResponseEntity<ApiResponse<Page<BookmarkResponse>>> getUserBookmarks(
            @Parameter(description = "사용자명") @PathVariable String username,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        log.info("사용자 북마크 목록 조회 요청 - 사용자: {}", username);

        Page<BookmarkResponse> bookmarks = bookmarkService.getUserBookmarks(username, pageable);

        return ResponseEntity.ok(ApiResponse.success("사용자 북마크 목록입니다.", bookmarks));
    }

    /**
     * 특정 영화의 북마크 여부 확인
     * 북마크 버튼의 상태(추가/삭제) 결정에 사용 (아이콘으로 표시)
     *
     * @param movieId 확인할 영화 ID
     * @param userDetails 인증된 사용자 정보
     * @return 북마크 존재 여부 (true/false)
     */
    @Operation(summary = "북마크 여부 확인", description = "특정 영화의 북마크 여부를 확인합니다.")
    @SecurityRequirement(name = "Bearer Authentication")
    @GetMapping("/movies/{movieId}/check")
    public ResponseEntity<ApiResponse<Boolean>> checkBookmark(
            @Parameter(description = "영화 ID") @PathVariable Long movieId,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        boolean isBookmarked = bookmarkService.isBookmarked(userDetails.getId(), movieId);

        return ResponseEntity.ok(ApiResponse.success("북마크 여부입니다.", isBookmarked));
    }

    /**
     * 특정 장르의 북마크 목록 조회
     * 사용자가 북마크한 영화를 장르별로 필터링하여 조회
     *
     * @param genreId 조회할 장르 ID
     * @param userDetails 인증된 사용자 정보
     * @return 해당 장르의 북마크 목록
     */
    @Operation(summary = "장르별 북마크", description = "특정 장르의 북마크 목록을 조회합니다.")
    @SecurityRequirement(name = "Bearer Authentication")
    @GetMapping("/genre/{genreId}")
    public ResponseEntity<ApiResponse<List<BookmarkResponse>>> getBookmarksByGenre(
            @Parameter(description = "장르 ID") @PathVariable Long genreId,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        log.info("장르별 북마크 조회 요청 - 사용자 ID: {}, 장르 ID: {}", userDetails.getId(), genreId);

        List<BookmarkResponse> bookmarks = bookmarkService.getBookmarksByGenre(
                userDetails.getId(), genreId);

        return ResponseEntity.ok(ApiResponse.success("장르별 북마크 목록입니다.", bookmarks));
    }

    /**
     * 가장 많이 북마크된 영화 순위 조회
     * 인기 영화 랭킹이나 추천 시스템에서 활용
     *
     * @param limit 조회할 영화 수 (기본: 10개)
     * @return 북마크 수 기준 상위 영화 목록과 통계
     */
    @Operation(summary = "인기 북마크 영화", description = "가장 많이 북마크된 영화 순위를 조회합니다.")
    @GetMapping("/popular")
    public ResponseEntity<ApiResponse<List<MovieBookmarkStatsResponse>>> getMostBookmarkedMovies(
            @Parameter(description = "조회할 영화 수") @RequestParam(defaultValue = "10") int limit) {

        log.info("인기 북마크 영화 조회 요청 - 개수: {}", limit);

        List<MovieBookmarkStatsResponse> stats =
                bookmarkService.getMostBookmarkedMovies(limit);

        return ResponseEntity.ok(ApiResponse.success("인기 북마크 영화 순위입니다.", stats));
    }

    /**
     * 최근 북마크된 영화 목록 조회
     * 실시간 사용자 활동 표시에 활용
     *
     * @param limit 조회할 북마크 수 (기본: 10개)
     * @return 최근 생성된 북마크 목록
     */
    @Operation(summary = "최근 북마크", description = "최근에 북마크된 영화 목록을 조회합니다.")
    @GetMapping("/recent")
    public ResponseEntity<ApiResponse<List<BookmarkResponse>>> getRecentBookmarks(
            @Parameter(description = "조회할 북마크 수") @RequestParam(defaultValue = "10") int limit) {

        log.info("최근 북마크 조회 요청 - 개수: {}", limit);

        List<BookmarkResponse> bookmarks = bookmarkService.getRecentBookmarks(limit);

        return ResponseEntity.ok(ApiResponse.success("최근 북마크 목록입니다.", bookmarks));
    }

    /**
     * 북마크 관련 전체 통계 조회
     * 서비스 현황 파악에 활용
     *
     * @return 북마크 통계 정보 (전체 북마크 수 등)
     */
    @Operation(summary = "북마크 통계", description = "북마크 관련 통계를 조회합니다.")
    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<BookmarkStatsResponse>> getBookmarkStats() {

        log.info("북마크 통계 조회 요청");

        BookmarkStatsResponse stats = bookmarkService.getBookmarkStats();

        return ResponseEntity.ok(ApiResponse.success("북마크 통계 정보입니다.", stats));
    }
}
