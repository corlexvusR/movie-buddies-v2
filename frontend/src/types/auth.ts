/**
 * 로그인 요청 타입 (백엔드 LoginRequest와 일치)
 */
export interface LoginRequest {
  username: string;
  password: string;
}

/**
 * 회원가입 요청 타입 (백엔드 SignupRequest와 일치)
 */
export interface SignupRequest {
  username: string;
  email: string;
  nickname: string;
  password: string; // password1, password2 대신 단일 password
}

/**
 * JWT 응답 타입 (백엔드 JwtResponse와 일치)
 */
export interface JwtResponse {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
  user: UserResponse;
}

/**
 * 토큰 갱신 요청 타입 (백엔드 RefreshTokenRequest와 일치)
 */
export interface RefreshTokenRequest {
  refreshToken: string;
}

/**
 * 비밀번호 변경 요청 타입 (백엔드 PasswordChangeRequest와 일치)
 */
export interface PasswordChangeRequest {
  currentPassword: string;
  newPassword: string;
  confirmPassword: string;
}

/**
 * 인증 상태 타입
 */
export interface AuthState {
  isAuthenticated: boolean;
  user: UserResponse | null;
  token: string | null;
  refreshToken: string | null;
  loading: boolean;
  error: string | null;
  isInitialized: boolean;
}

/**
 * 사용자 응답 타입 (백엔드 UserResponse와 일치)
 */
export interface UserResponse {
  id: number;
  username: string;
  email: string;
  nickname: string;
  profileImageUrl?: string;
  createdAt: string;
  lastLoginAt?: string;
}
