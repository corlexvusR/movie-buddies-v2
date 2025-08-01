package com.moviebuddies.controller;

import com.moviebuddies.dto.request.UserUpdateRequest;
import com.moviebuddies.dto.response.ApiResponse;
import com.moviebuddies.dto.response.UserResponse;
import com.moviebuddies.security.UserDetailsImpl;
import com.moviebuddies.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 사용자 관리 REST API 컨트롤러
 *
 * UserController는 사용자 프로필 관리와 관련된 모든 엔드포인트를 제공
 * - 사용자 검색 및 프로필 조회
 * - 프로필 정보 수정
 * - 프로필 이미지 업로드
 * - 계정 삭제
 */
@Slf4j
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Tag(name = "User", description = "사용자 관리 API")
public class UserController {

    /**
     * 사용자 서비스 - 사용자 관련 비즈니스 로직 처리
     */
    private final UserService userService;

    /**
     * 사용자 검색 API
     * 
     * 닉네임, 사용자명, 이메일을 기준으로 사용자를 검색
     * 친구 추가 기능에서 다른 사용자를 찾을 때 사용
     * 자신은 검색 결과에서 제외
     *
     * @param query 검색 키워드 (닉네임, 사용자명, 이메일 중 하나)
     * @param pageable 페이징 정보 (기본 10개씩)
     * @param userDetails 현재 로그인한 사용자 정보
     * @return 검색한 사용자 목록 (페이징 처리됨)
     */
    @Operation(summary = "사용자 검색", description = "닉네임, 사용자명, 이메일로 사용자를 검색합니다.")
    @SecurityRequirement(name = "Bearer Authentication")
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<Page<UserResponse>>> searchUsers(@RequestParam String query, @PageableDefault(size = 10) Pageable pageable, @AuthenticationPrincipal UserDetailsImpl userDetails) {

        log.info("사용자 검색 요청: query={}, userId={}", query, userDetails.getId());

        Page<UserResponse> users = userService.searchUsers(query, userDetails.getId(), pageable);
        return ResponseEntity.ok(ApiResponse.success("사용자 검색 결과입니다.", users));
    }

    /**
     * 내 프로필 조회 API
     *
     * 현재 로그인한 사용자의 상세 프로필 정보를 조회
     * 다른 사용자에게는 공개되지 않는 민감한 정보(이메일 등)도 포함
     *
     * @param userDetails 현재 로그인한 사용자 정보
     * @return 내 프로필 정보 (모든 필드 포함)
     */
    @Operation(summary = "내 프로필 조회", description = "로그인한 사용자의 프로필 정보를 조회합니다.")
    @SecurityRequirement(name = "Bearer Authentication")
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> getMyProfile(@AuthenticationPrincipal UserDetailsImpl userDetails) {

        log.info("내 프로필 요청: userId={}", userDetails.getId());

        UserResponse user = userService.getUserById(userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success("내 프로필 정보입니다.", user));
    }

    /**
     * 사용자 프로필 조회 API (공개되는 정보)
     *
     * 특정 사용자의 공개 프로필 정보를 조회
     * 인증 없이도 접근 가능하며, 공개 정보만 반환
     * 사용자 프로필 페이지에서 사용
     *
     * @param username 조회할 사용자의 사용자명
     * @return 공개 프로필 정보 (민감 정보 제외)
     */
    @Operation(summary = "사용자 프로필 조회", description = "특정 사용자의 프로필 정보를 조회합니다.")
    @GetMapping("/{username}")
    public ResponseEntity<ApiResponse<UserResponse>> getUserProfile(@PathVariable String username) {

        log.info("사용자 프로필 조회 요청: userName={}", username);

        UserResponse user = userService.getUserByUsername(username);
        return ResponseEntity.ok(ApiResponse.success("사용자 프로필 정보입니다.", user));
    }

    /**
     * 프로필 수정 API
     *
     * 사용자의 프로필 정보를 수정
     * 닉네임 등 개인정보를 업데이트할 수 있음
     * 중복 닉네임 검증 등은 서비스 레이어에서 처리
     *
     * @param request 수정할 프로필 정보
     * @param userDetails 현재 로그인한 사용자 정보
     * @return 수정된 프로필 정보
     */
    @Operation(summary = "프로필 수정", description = "사용자 프로필 정보를 수정합니다.")
    @SecurityRequirement(name = "Bearer Authentication")
    @PutMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> updateProfile(@Valid @RequestBody UserUpdateRequest request, @AuthenticationPrincipal UserDetailsImpl userDetails) {

        log.info("프로필 수정 요청: userId={}", userDetails.getId());

        UserResponse user = userService.updateUser(userDetails.getId(), request);
        return ResponseEntity.ok(ApiResponse.success("프로필이 수정되었습니다.", user));
    }

    /**
     * 프로필 이미지 업로드 API
     *
     * 사용자의 프로필 이미지를 업로드하고 업데이트
     * 파일 크기, 형식 검증은 서비스 레이어에서 처리
     * 기존 이미지가 있다면 삭제 후 새 이미지로 교체
     *
     * @param file 프로필 이미지 파일 (JPEG, PNG 등)
     * @param userDetails 현재 로그인한 사용자 정보
     * @return 업로드된 이미지의 URL
     */
    @Operation(summary = "프로필 이미지 업로드", description = "프로필 이미지를 업로드합니다.")
    @SecurityRequirement(name = "Bearer Authentication")
    @PostMapping("/me/profile-image")
    public ResponseEntity<ApiResponse<String>> uploadProfileImage(@RequestParam("file")MultipartFile file, @AuthenticationPrincipal UserDetailsImpl userDetails) {

        log.info("프로필 이미지 업로드 요청: userId={}, fileName={}", userDetails.getId(), file.getOriginalFilename());

        String imageUrl = userService.updateProfileImage(userDetails.getId(), file);
        return ResponseEntity.ok(ApiResponse.success("프로필 이미지가 업로드되었습니다.", imageUrl));
    }

    /**
     * 계정 삭제 API (소프트 삭제)
     * 
     * 사용자 계정을 비활성화 상태로 변경
     * 실제 데이터는 삭제되지 않고 isActive=false로 설정되어, 로그인 및 서비스 이용이 불가능해짐
     * 프로필 이미지만 실제로 삭제되어 저장 공간을 절약
     * 향후 계정 복구나 데이터 분석을 위해 사용자 데이터는 보존됨
     *
     * @param userDetails 현재 로그인한 사용자 정보
     * @return 비활성화 완료 메시지
     */
    @Operation(summary = "계정 삭제", description = "사용자 계정을 삭제합니다.")
    @SecurityRequirement(name = "Bearer Authentication")
    @DeleteMapping("/me")
    public ResponseEntity<ApiResponse<Void>> deleteAccount(@AuthenticationPrincipal UserDetailsImpl userDetails) {

        log.warn("계정 삭제(비활성화) 요청: userId={}", userDetails.getId());

        userService.deleteUser(userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success("계정이 삭제되었습니다."));
    }
}
