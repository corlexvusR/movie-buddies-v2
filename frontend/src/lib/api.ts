import axios, {
  AxiosResponse,
  AxiosError,
  InternalAxiosRequestConfig,
} from "axios";
import { ApiResponse } from "@/types/api";

// 재시도 요청 인터페이스 정의
interface RetryableRequest extends InternalAxiosRequestConfig {
  _retry?: boolean;
}

// API 클라이언트 인스턴스 생성
const api = axios.create({
  baseURL: process.env.NEXT_PUBLIC_API_URL || "http://localhost:8080",
  timeout: 10000,
  headers: {
    "Content-Type": "application/json",
  },
});

// 요청 인터셉터 - 인증 토큰 자동 추가
api.interceptors.request.use(
  (config) => {
    // 클라이언트 사이드에서만 localStorage 접근
    if (typeof window !== "undefined") {
      const token = localStorage.getItem("token");
      if (token) {
        config.headers.Authorization = `Bearer ${token}`;
      }
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// 응답 인터셉터 - 에러 처리 및 토큰 갱신
api.interceptors.response.use(
  (response: AxiosResponse) => {
    return response;
  },
  async (error: AxiosError) => {
    const originalRequest = error.config as RetryableRequest;

    // 401 에러 (인증 만료) 처리
    if (
      error.response?.status === 401 &&
      originalRequest &&
      !originalRequest._retry
    ) {
      originalRequest._retry = true;

      try {
        // 토큰 갱신 시도
        const refreshToken = localStorage.getItem("refreshToken");
        if (refreshToken) {
          const response = await axios.post(
            `${process.env.NEXT_PUBLIC_API_URL}/auth/refresh`,
            { refreshToken }
          );

          const { accessToken, refreshToken: newRefreshToken } =
            response.data.data;

          localStorage.setItem("token", accessToken);
          localStorage.setItem("refreshToken", newRefreshToken);

          // 원래 요청 재시도
          originalRequest.headers.Authorization = `Bearer ${accessToken}`;
          return api(originalRequest);
        }
      } catch (refreshError) {
        // 토큰 갱신 실패 시 로그아웃 처리
        localStorage.removeItem("token");
        localStorage.removeItem("refreshToken");
        window.location.href = "/auth/login";
        return Promise.reject(refreshError);
      }
    }

    return Promise.reject(error);
  }
);

// API 응답 타입 가드
export function isApiResponse<T>(data: unknown): data is ApiResponse<T> {
  return (
    typeof data === "object" &&
    data !== null &&
    typeof (data as Record<string, unknown>).success === "boolean" &&
    typeof (data as Record<string, unknown>).message === "string"
  );
}

// API 호출 래퍼 함수들
export const apiClient = {
  // GET 요청
  get: async <T>(url: string, params?: Record<string, unknown>): Promise<T> => {
    const response = await api.get<ApiResponse<T>>(url, { params });

    if (isApiResponse<T>(response.data)) {
      if (response.data.success) {
        return response.data.data as T;
      } else {
        throw new Error(response.data.message);
      }
    }

    return response.data as T;
  },

  // POST 요청
  post: async <T>(url: string, data?: unknown): Promise<T> => {
    const response = await api.post<ApiResponse<T>>(url, data);

    if (isApiResponse<T>(response.data)) {
      if (response.data.success) {
        return response.data.data as T;
      } else {
        throw new Error(response.data.message);
      }
    }

    return response.data as T;
  },

  // PUT 요청
  put: async <T>(url: string, data?: unknown): Promise<T> => {
    const response = await api.put<ApiResponse<T>>(url, data);

    if (isApiResponse<T>(response.data)) {
      if (response.data.success) {
        return response.data.data as T;
      } else {
        throw new Error(response.data.message);
      }
    }

    return response.data as T;
  },

  // PATCH 요청
  patch: async <T>(url: string, data?: unknown): Promise<T> => {
    const response = await api.patch<ApiResponse<T>>(url, data);

    if (isApiResponse<T>(response.data)) {
      if (response.data.success) {
        return response.data.data as T;
      } else {
        throw new Error(response.data.message);
      }
    }

    return response.data as T;
  },

  // DELETE 요청
  delete: async <T>(url: string): Promise<T> => {
    const response = await api.delete<ApiResponse<T>>(url);

    if (isApiResponse<T>(response.data)) {
      if (response.data.success) {
        return response.data.data as T;
      } else {
        throw new Error(response.data.message);
      }
    }

    return response.data as T;
  },

  // 파일 업로드
  upload: async <T>(url: string, formData: FormData): Promise<T> => {
    const response = await api.post<ApiResponse<T>>(url, formData, {
      headers: {
        "Content-Type": "multipart/form-data",
      },
    });

    if (isApiResponse<T>(response.data)) {
      if (response.data.success) {
        return response.data.data as T;
      } else {
        throw new Error(response.data.message);
      }
    }

    return response.data as T;
  },
};

export default api;
