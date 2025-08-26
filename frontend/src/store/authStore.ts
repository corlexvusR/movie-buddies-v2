import { create } from "zustand";
import { persist } from "zustand/middleware";
import {
  AuthState,
  LoginRequest,
  SignupRequest,
  JwtResponse,
  UserResponse,
} from "@/types/auth";
import { apiClient } from "@/lib/api";

interface AuthActions {
  login: (credentials: LoginRequest) => Promise<void>;
  signup: (userData: SignupRequest) => Promise<void>;
  logout: () => void;
  refreshAuthToken: () => Promise<void>;
  fetchUser: () => Promise<void>;
  updateUser: (user: UserResponse) => void;
  clearError: () => void;
  setLoading: (loading: boolean) => void;
}

type AuthStore = AuthState & AuthActions;

export const useAuthStore = create<AuthStore>()(
  persist(
    (set, get) => ({
      // 초기 상태
      isAuthenticated: false,
      user: null,
      token: null,
      refreshToken: null,
      loading: false,
      error: null,

      // 로그인
      login: async (credentials: LoginRequest) => {
        try {
          set({ loading: true, error: null });

          const response = await apiClient.post<JwtResponse>(
            "/auth/login",
            credentials
          );

          // 토큰 저장 (클라이언트 사이드에서만)
          if (typeof window !== "undefined") {
            localStorage.setItem("token", response.accessToken);
            localStorage.setItem("refreshToken", response.refreshToken);
          }

          set({
            isAuthenticated: true,
            user: response.user,
            token: response.accessToken,
            refreshToken: response.refreshToken,
            loading: false,
          });
        } catch (error: unknown) {
          const errorMessage =
            error instanceof Error ? error.message : "로그인에 실패했습니다.";
          set({
            loading: false,
            error: errorMessage,
          });
          throw error;
        }
      },

      // 회원가입
      signup: async (userData: SignupRequest) => {
        try {
          set({ loading: true, error: null });

          const signupData = {
            username: userData.username,
            email: userData.email,
            nickname: userData.nickname,
            password: userData.password,
          };

          await apiClient.post("/auth/signup", signupData);
          set({ loading: false });
        } catch (error: unknown) {
          const errorMessage =
            error instanceof Error ? error.message : "회원가입에 실패했습니다.";
          set({
            loading: false,
            error: errorMessage,
          });
          throw error;
        }
      },

      // 로그아웃
      logout: () => {
        if (typeof window !== "undefined") {
          localStorage.removeItem("token");
          localStorage.removeItem("refreshToken");
        }

        set({
          isAuthenticated: false,
          user: null,
          token: null,
          refreshToken: null,
          error: null,
        });
      },

      // 토큰 갱신
      refreshAuthToken: async () => {
        try {
          const refreshToken =
            get().refreshToken ||
            (typeof window !== "undefined"
              ? localStorage.getItem("refreshToken")
              : null);

          if (!refreshToken) {
            get().logout();
            return;
          }

          const response = await apiClient.post<JwtResponse>("/auth/refresh", {
            refreshToken,
          });

          if (typeof window !== "undefined") {
            localStorage.setItem("token", response.accessToken);
            localStorage.setItem("refreshToken", response.refreshToken);
          }

          set({
            token: response.accessToken,
            refreshToken: response.refreshToken,
            user: response.user,
          });
        } catch (error) {
          get().logout();
          throw error;
        }
      },

      // 사용자 정보 가져오기
      fetchUser: async () => {
        try {
          const user = await apiClient.get<UserResponse>("/users/me");
          set({ user });
        } catch (error: unknown) {
          const axiosError = error as { response?: { status: number } };
          if (axiosError.response?.status === 401) {
            get().logout();
          }
          throw error;
        }
      },

      // 사용자 정보 업데이트
      updateUser: (user: UserResponse) => {
        set({ user });
      },

      // 에러 클리어
      clearError: () => {
        set({ error: null });
      },

      // 로딩 상태 설정
      setLoading: (loading: boolean) => {
        set({ loading });
      },
    }),
    {
      name: "auth-storage",
      partialize: (state) => ({
        isAuthenticated: state.isAuthenticated,
        user: state.user,
        token: state.token,
        refreshToken: state.refreshToken,
      }),
    }
  )
);

// 초기화 함수 (앱 시작 시 호출)
export const initializeAuth = () => {
  const state = useAuthStore.getState();

  if (state.token) {
    state.fetchUser().catch(() => {
      state.logout();
    });
  }
};
