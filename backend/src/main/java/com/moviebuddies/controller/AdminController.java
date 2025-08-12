package com.moviebuddies.controller;

import com.moviebuddies.dto.response.ApiResponse;
import com.moviebuddies.service.TmdbService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 관리자 전용 API 컨트롤러
 *
 * TMDB 데이터 동기화 기능을 제공
 * 인증된 사용자만 접근할 수 있으며, 초기 데이터 구축이나 정기적인 데이터 업데이트 시 사용
 * 나중에 관리자 계정만 사용할 수 있도록 구현할 수 있음
 *
 * - TMDB 전체 데이터 동기화 (장르, 영화, 배우)
 * - 장르 데이터만 개별 동기화
 */
@Slf4j
@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@Tag(name = "Admin", description = "관리자 API")
public class AdminController {

    /**
     * TMDB API 연동 서비스
     * 외부 TMDB API에서 영화 데이터를 가져와 로컬 데이터베이스에 동기화
     */
    private final TmdbService tmdbService;

    /**
     * TMDB 전체 데이터 동기화
     *
     * 장르, 인기 영화, 현재 상영작, 배우 정보를 순차적으로 동기화
     * 초기 데이터베이스 구축 시나 대규모 업데이트 시 사용
     *
     * @return 동기화 완료 메시지
     */
    @Operation(summary = "TMDB 데이터 동기화", description = "장르, 영화, 배우 정보를 동기화합니다.")
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "데이터 동기화 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "동기화 중 오류 발생")
    })
    @PostMapping("/tmdb/sync-all")
    public ResponseEntity<ApiResponse<String>> syncAllData() {

        log.info("DB에 필요한 TMDB 데이터를 동기화합니다.");

        try {
            tmdbService.syncAllData();

            log.info("TMDB 데이터 동기화가 완료되었습니다.");

            return ResponseEntity.ok(ApiResponse.success("데이터 동기화가 완료되었습니다."));

        } catch (Exception e) {

            log.error("TMDB 데이터 동기화 중 오류가 발생했습니다.", e);

            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("데이터 동기화 중 오류가 발생했습니다: " + e.getMessage()));
        }
    }

    /**
     * TMDB 장르 데이터 동기화
     *
     * 영화 장르 정보만을 개별적으로 동기화
     *
     * @return 장르 동기화 완료 메시지
     */
    @Operation(summary = "장르 데이터 동기화", description = "TMDB 장르 정보만 동기화합니다.")
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "장르 동기화 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "동기화 중 오류 발생")
    })
    @PostMapping("/tmdb/sync-genres")
    public ResponseEntity<ApiResponse<String>> syncGenres() {

        log.info("DB에 필요한 TMDB 장르 데이터를 동기화합니다.");

        try {
            tmdbService.syncGenres();

            log.info("TMDB 장르 데이터 동기화가 완료되었습니다.");

            return ResponseEntity.ok(ApiResponse.success("장르 동기화가 완료되었습니다."));

        } catch (Exception e) {

            log.error("TMDB 장르 데이터 동기화 중 오류가 발생했습니다.", e);

            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("장르 동기화 중 오류가 발생했습니다: " + e.getMessage()));
        }
    }
}
