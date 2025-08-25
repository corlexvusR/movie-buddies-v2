import { jwtDecode } from "jwt-decode";
import { STORAGE_KEYS } from "./constants";

/**
 * JWT 토큰 디코딩 타입
 */
interface JwtPayload {
  sub: string; // username
  exp: number; // expiration time
  iat: number; // issued at
  userId: number;
}

/**
 * 로컬스토리지에서 액세스 토큰 가져오기
 */
export const getToken = (): string | null => {
  if (typeof window === "undefined") return null;
  return localStorage.getItem(STORAGE_KEYS.TOKEN);
};

/**
 * 로컬스토리지에서 리프레시 토큰 가져오기
 */
export const getRefreshToken = (): string | null => {
  if (typeof window === "undefined") return null;
  return localStorage.getItem(STORAGE_KEYS.REFRESH_TOKEN);
};

/**
 * 토큰을 로컬스토리지에 저장
 */
export const setTokens = (accessToken: string, refreshToken: string): void => {
  if (typeof window === "undefined") return;

  localStorage.setItem(STORAGE_KEYS.TOKEN, accessToken);
  localStorage.setItem(STORAGE_KEYS.REFRESH_TOKEN, refreshToken);
};

/**
 * 모든 인증 토큰 제거
 */
export const removeTokens = (): void => {
  if (typeof window === "undefined") return;

  localStorage.removeItem(STORAGE_KEYS.TOKEN);
  localStorage.removeItem(STORAGE_KEYS.REFRESH_TOKEN);
  localStorage.removeItem(STORAGE_KEYS.USER);
};

/**
 * JWT 토큰 디코딩 및 유효성 검사
 */
export const decodeToken = (token: string): JwtPayload | null => {
  try {
    return jwtDecode<JwtPayload>(token);
  } catch (error) {
    console.warn("토큰 디코딩 실패:", error);
    return null;
  }
};

/**
 * 토큰 만료 여부 확인
 */
export const isTokenExpired = (token: string): boolean => {
  const decoded = decodeToken(token);
  if (!decoded) return true;

  const currentTime = Math.floor(Date.now() / 1000);
  return decoded.exp < currentTime;
};

/**
 * 토큰에서 사용자 ID 추출
 */
export const getUserIdFromToken = (token: string): number | null => {
  const decoded = decodeToken(token);
  return decoded?.userId || null;
};

/**
 * 토큰에서 사용자명 추출
 */
export const getUsernameFromToken = (token: string): string | null => {
  const decoded = decodeToken(token);
  return decoded?.sub || null;
};

/**
 * 현재 사용자 인증 상태 확인
 */
export const isAuthenticated = (): boolean => {
  const token = getToken();
  if (!token) return false;

  return !isTokenExpired(token);
};

/**
 * 토큰 자동 갱신이 필요한지 확인
 * 만료 5분 전부터 갱신 권장
 */
export const shouldRefreshToken = (token?: string): boolean => {
  const accessToken = token || getToken();
  if (!accessToken) return false;

  const decoded = decodeToken(accessToken);
  if (!decoded) return false;

  const currentTime = Math.floor(Date.now() / 1000);
  const timeUntilExpiry = decoded.exp - currentTime;

  // 5분(300초) 이전부터 갱신
  return timeUntilExpiry < 300;
};

/**
 * Authorization 헤더 값 생성
 */
export const getAuthHeader = (): string | null => {
  const token = getToken();
  return token ? `Bearer ${token}` : null;
};

/**
 * 로그인 페이지로 리다이렉트
 */
export const redirectToLogin = (returnUrl?: string): void => {
  if (typeof window === "undefined") return;

  const loginUrl = "/auth/login";
  const redirectUrl = returnUrl || window.location.pathname;

  if (redirectUrl && redirectUrl !== "/") {
    window.location.href = `${loginUrl}?redirect=${encodeURIComponent(
      redirectUrl
    )}`;
  } else {
    window.location.href = loginUrl;
  }
};

/**
 * 로그아웃 후 처리 (토큰 제거 + 홈으로 리다이렉트)
 */
export const handleLogout = (): void => {
  removeTokens();

  if (typeof window !== "undefined") {
    window.location.href = "/";
  }
};

/**
 * 토큰 만료 시 자동 로그아웃 처리
 */
export const handleTokenExpired = (): void => {
  console.warn("토큰이 만료되었습니다. 다시 로그인해주세요.");
  removeTokens();
  redirectToLogin();
};

/**
 * 보호된 라우트 접근 가능 여부 확인
 */
export const canAccessProtectedRoute = (): boolean => {
  return isAuthenticated();
};

/**
 * 인증이 필요한 작업 전 토큰 상태 확인
 */
export const ensureAuthenticated = (): boolean => {
  if (!isAuthenticated()) {
    redirectToLogin();
    return false;
  }
  return true;
};
