/**
 * React Query를 위한 글로벌 상태 관리 Provider
 * 인증 상태 초기화 및 토스트 컨테이너 포함
 */

"use client";

import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { useState, useEffect } from "react";
import { initializeAuth } from "@/store/authStore";
import { ToastContainer } from "./ToastContainer";

export function Providers({ children }: { children: React.ReactNode }) {
  const [queryClient] = useState(
    () =>
      new QueryClient({
        defaultOptions: {
          queries: {
            staleTime: 60 * 1000, // 1분
            gcTime: 10 * 60 * 1000, // 10분 (이전 cacheTime)
            retry: (failureCount: number, error: Error) => {
              // HTTP 클라이언트 에러(4xx)는 재시도하지 않음
              const httpError = error as Error & {
                response?: { status: number };
              };

              if (
                httpError.response?.status &&
                httpError.response.status >= 400 &&
                httpError.response.status < 500
              ) {
                return false;
              }
              return failureCount < 3;
            },
          },
          mutations: {
            retry: false,
          },
        },
      })
  );

  // 앱 초기화 시 인증 상태 복원
  useEffect(() => {
    initializeAuth();
  }, []);

  return (
    <QueryClientProvider client={queryClient}>
      {children}
      <ToastContainer />
    </QueryClientProvider>
  );
}
