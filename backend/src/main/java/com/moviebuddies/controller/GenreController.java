package com.moviebuddies.controller;

import com.moviebuddies.dto.response.ApiResponse;
import com.moviebuddies.dto.response.GenreResponse;
import com.moviebuddies.service.GenreService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 장르 관련 REST API 컨트롤러
 * 영화 필터링과 정렬을 위한 장르 정보 조회 기능을 HTTP 엔드포인트로 제공
 */
@Slf4j
@RestController
@RequestMapping("/movies/genres")
@RequiredArgsConstructor
@Tag(name = "Genre", description = "장르 관련 API")
public class GenreController {

    private final GenreService genreService;

    /**
     * 모든 장르 목록 조회
     * 영화 필터링 UI에서 장르 선택 옵션을 제공하기 위해 사용
     * 영화 수가 많은 순으로 정렬되어 영화 수가 많은 장르가 먼저 표시됨
     *
     * @return 모든 장르 목록과 성공 메시지
     */
    @Operation(summary = "전체 장르 목록 조회", description = "모든 장르를 영화 수가 많은 순으로 조회합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공")
    })
    @GetMapping
    public ResponseEntity<ApiResponse<List<GenreResponse>>> getAllGenres() {
        log.info("전체 장르 목록 조회 요청");

        List<GenreResponse> genres = genreService.getAllGenres();

        return ResponseEntity.ok(ApiResponse.success("장르 목록을 성공적으로 조회했습니다.", genres));
    }

    /**
     * 특정 장르 정보 조회
     * 장르 상세 페이지나 장르 정보 확인에 사용
     *
     * @param genreId 조회할 장르의 고유 ID
     * @return 장르 상세 정보와 성공 메시지
     */
    @Operation(summary = "장르 상세 정보 조회", description = "특정 장르의 상세 정보를 조회합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "장르를 찾을 수 없음")
    })
    @GetMapping("/{genreId}")
    public ResponseEntity<ApiResponse<GenreResponse>> getGenre(
            @Parameter(description = "장르 ID")
            @PathVariable Long genreId) {

        log.info("장르 상세 정보 조회 요청 - ID: {}", genreId);

        GenreResponse genre = genreService.getGenre(genreId);

        return ResponseEntity.ok(ApiResponse.success("장르 정보를 성공적으로 조회했습니다.", genre));
    }

    /**
     * 장르명으로 장르 조회
     * 장르 이름을 통한 검색이나 특정 장르 정보 조회에 사용
     *
     * @param name 장르명
     * @return 장르 정보와 성공 메시지
     */
    @Operation(summary = "장르명으로 조회", description = "장르명을 통해 장르 정보를 조회합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "장르를 찾을 수 없음")
    })
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<GenreResponse>> getGenreByName(
            @Parameter(description = "장르명")
            @RequestParam String name) {

        log.info("장르명으로 조회 요청 - 이름: {}", name);

        GenreResponse genre = genreService.getGenreByName(name);

        return ResponseEntity.ok(ApiResponse.success("장르 정보를 성공적으로 조회했습니다.", genre));
    }

    /**
     * 특정 영화에 속한 장르들 조회
     * 영화 상세 페이지에서 해당 영화의 장르 태그 표시에 사용
     *
     * @param movieId 영화 ID
     * @return 해당 영화의 장르 목록과 성공 메시지
     */
    @Operation(summary = "영화별 장르 조회", description = "특정 영화에 속한 모든 장르를 조회합니다.")
    @GetMapping("/movie/{movieId}")
    public ResponseEntity<ApiResponse<List<GenreResponse>>> getGenresByMovieId(
            @Parameter(description = "영화 ID")
            @PathVariable Long movieId) {

        log.info("영화별 장르 조회 요청 - 영화 ID: {}", movieId);

        List<GenreResponse> genres = genreService.getGenresByMovieId(movieId);

        return ResponseEntity.ok(ApiResponse.success("영화의 장르 목록을 성공적으로 조회했습니다.", genres));
    }
}