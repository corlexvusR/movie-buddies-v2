package com.moviebuddies.controller;

import com.moviebuddies.dto.request.LoginRequest;
import com.moviebuddies.dto.request.RefreshTokenRequest;
import com.moviebuddies.dto.request.SignupRequest;
import com.moviebuddies.dto.response.ApiResponse;
import com.moviebuddies.dto.response.JwtResponse;
import com.moviebuddies.dto.response.UserResponse;
import com.moviebuddies.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 인증 관련 API 컨트롤러
 *
 * 사용자 인증과 관련된 모든 엔드포인트를 제공
 * - 회원가입 및 로그인
 * - JWT 토큰 발급 및 갱신
 * - 사용자 정보 중복 확인
 * - 로그아웃
 */
@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "인증 관련 API")
public class AuthController {

    /**
     * 인증 서비스 - 비즈니스 로직 처리를 담당
     */
    private final AuthService authService;

    /**
     * 회원가입 API
     *
     * 새로운 사용자 계정을 생성
     * 요청 데이터의 유효성 검증을 수행하고, 중복 확인 후 계정을 생성
     *
     * @param request 회원가입 요청 데이터 (사용자명, 이메일, 비밀번호, 닉네임 등)
     * @return 생성된 사용자 정보와 성공 메시지
     */
    @Operation(summary = "회원가입", description = "새로운 사용자 계정을 생성합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "회원가입 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "중복된 정보")
    })
    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<UserResponse>> signup(@Valid @RequestBody SignupRequest request) {
        log.info("회원가입 요청: {}", request.getUsername());

        UserResponse userResponse = authService.signup(request);
        
        // 201 Created 상태코드와 함께 응담
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("회원가입이 완료되었습니다.", userResponse));
    }

    /**
     * 로그인 API
     * 
     * 사용자 인증을 수행하고 JWT 토큰을 발급
     * 액세스 토큰과 리프레시 토큰을 모두 포함한 응답을 반환
     * 
     * @param request 로그인 요청 데이터 (사용자명/이메일, 비밀번호)
     * @return JWT 토큰 정보 (액세스 토큰, 리프레시 토큰, 만료 시간)
     */
    @Operation(summary = "로그인", description = "사용자 인증 후 JWL 토큰을 발급합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "로그인 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<JwtResponse>> login(@Valid @RequestBody LoginRequest request) {
        log.info("로그인 요청: {}", request.getUsername());

        JwtResponse jwtResponse = authService.login(request);

        return ResponseEntity.ok(ApiResponse.success("로그인이 완료되었습니다.", jwtResponse));
    }

    /**
     * 토큰 갱신 API
     *
     * 리프레시 토큰을 사용하여 새로운 액세스 토큰을 발급
     * 액세스 토큰이 만료되었을 때 재인증 없이 토큰을 갱신
     * 
     * @param request 리프레시 토큰 요청 데이터
     * @return 새로운 JWT 토큰 정보
     */
    @Operation(summary = "토큰 새로 고침", description = "리프레시 토큰을 사용해 새로운 액세스 토큰을 발급합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "토큰 새로고침 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "유효하지 않은 토큰")
    })
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<JwtResponse>> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        log.info("토큰 새로고침 요청");

        JwtResponse jwtResponse = authService.refreshToken(request.getRefreshToken());

        return ResponseEntity.ok(ApiResponse.success("토큰이 새로고침되었습니다.", jwtResponse));
    }

    /**
     * 사용자명 중복 확인 API
     *
     * 회원가입 시 사용자명의 사용 가능 여부를 실시간으로 확인
     * 프론트엔드에서 입력 필드 검증 시 사용
     *
     * @param username 확인할 사용자명
     * @return 사용 가능 여부 (true: 사용 가능, false: 이미 사용 중)
     */
    @Operation(summary = "사용자명 중복 확인", description = "사용자명 사용 가능 여부를 확인합니다.")
    @GetMapping("/check/username")
    public ResponseEntity<ApiResponse<Boolean>> checkUsername(@RequestParam String username) {
        boolean available = authService.isUsernameAvailable(username);
        String message = available ? "사용 가능한 사용자명입니다." : "이미 사용 중인 사용자명입니다.";

        return ResponseEntity.ok(ApiResponse.success(message, available));
    }

    /**
     * 이메일 중복 확인 API
     *
     * 회원가입 시 이메일의 사용 가능 여부를 실시간으로 확인
     * 이메일 형식 검증은 프론트엔드 또는 DTO에서 수행
     *
     * @param email 확인할 이메일 주소
     * @return 사용 가능 여부 (true: 사용 가능, false: 이미 사용 중)
     */
    @Operation(summary = "이메일 중복 확인", description = "이메일 사용 가능 여부를 확인합니다.")
    @GetMapping("/check/email")
    public ResponseEntity<ApiResponse<Boolean>> checkEmail(@RequestParam String email) {
        boolean available = authService.isEmailAvailable(email);
        String message = available ? "사용 가능한 이메일입니다." : "이미 사용 중인 이메일입니다.";

        return ResponseEntity.ok(ApiResponse.success(message, available));
    }

    /**
     * 닉네임 중복 확인 API
     *
     * 회원가입 시 닉네임의 사용 가능 여부를 실시간으로 확인
     * 닉네임은 사용자 프로필에서 표시되는 이름
     * 
     * @param nickname 확인할 닉네임
     * @return 사용 가능 여부 (true: 사용 가능, false: 이미 사용 중)
     */
    @Operation(summary = "닉네임 중복 확인", description = "닉네임 사용 가능 여부를 확인합니다.")
    @GetMapping("/check/nickname")
    public ResponseEntity<ApiResponse<Boolean>> checkNickname(@RequestParam String nickname) {
        boolean available = authService.isNicknameAvailable(nickname);
        String message = available ? "사용 가능한 닉네임입니다." : "이미 사용 중인 닉네임입니다.";

        return ResponseEntity.ok(ApiResponse.success(message, available));
    }

    /**
     * 로그아웃 API
     * 
     * 현재 사용자 세션을 종료
     * JWT 토큰 방식에서는 클라이언트 측에서 토큰을 삭제하는 것으로 로그아웃을 처리
     *
     * @return 로그아웃 완료 메시지
     */
    @Operation(summary = "로그아웃", description = "현재 세션을 종료합니다.")
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout() {
        // JWT 토큰 방식에서는 클라이언트에서 토큰을 삭제하면 됨
        log.info("로그아웃 요청");

        return ResponseEntity.ok(ApiResponse.success("로그아웃이 완료되었습니다."));
    }
}
