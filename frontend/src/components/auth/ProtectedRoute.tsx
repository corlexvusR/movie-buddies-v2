"use client";

import { useEffect } from "react";
import { useRouter, usePathname } from "next/navigation";
import { useAuth } from "@/hooks/useAuth";
import { Loading } from "@/components/ui/Loading";

interface ProtectedRouteProps {
  children: React.ReactNode;
  fallback?: React.ReactNode;
  redirectTo?: string;
}

export function ProtectedRoute({
  children,
  fallback,
  redirectTo = "/auth/login",
}: ProtectedRouteProps) {
  const { isAuthenticated, isLoading } = useAuth();
  const router = useRouter();
  const pathname = usePathname();

  useEffect(() => {
    if (!isLoading && !isAuthenticated) {
      const redirectUrl = `${redirectTo}?redirect=${encodeURIComponent(
        pathname
      )}`;
      router.replace(redirectUrl);
    }
  }, [isAuthenticated, isLoading, router, pathname, redirectTo]);

  if (isLoading) {
    return (
      fallback || (
        <div className="flex items-center justify-center min-h-screen">
          <Loading size="lg" text="로딩" />
        </div>
      )
    );
  }

  if (!isAuthenticated) {
    return (
      fallback || (
        <div className="flex items-center justify-center min-h-screen">
          <Loading size="lg" text="리다이렉트" />
        </div>
      )
    );
  }

  return <>{children}</>;
}

// 역방향 보호 (로그인한 사용자가 인증 페이지 접근 방지)
interface GuestOnlyRouteProps {
  children: React.ReactNode;
  redirectTo?: string;
}

export function GuestOnlyRoute({
  children,
  redirectTo = "/",
}: GuestOnlyRouteProps) {
  const { isAuthenticated, isLoading } = useAuth();
  const router = useRouter();

  useEffect(() => {
    if (!isLoading && isAuthenticated) {
      router.replace(redirectTo);
    }
  }, [isAuthenticated, isLoading, router, redirectTo]);

  if (isLoading) {
    return (
      <div className="flex items-center justify-center min-h-screen">
        <Loading size="lg" text="로딩" />
      </div>
    );
  }

  if (isAuthenticated) {
    return (
      <div className="flex items-center justify-center min-h-screen">
        <Loading size="lg" text="리다이렉트" />
      </div>
    );
  }

  return <>{children}</>;
}
