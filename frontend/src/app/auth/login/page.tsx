import { Suspense } from "react";
import { Metadata } from "next";
import { LoginForm } from "@/components/auth/LoginForm";
import { LoadingSpinner } from "@/components/ui/Loading";

export const metadata: Metadata = {
  title: "로그인 - MovieBuddies",
  description:
    "MovieBuddies에 로그인하여 영화 리뷰를 공유하고 친구들과 소통하세요!",
};

interface LoginPageProps {
  searchParams: {
    redirectTo?: string;
    message?: string;
  };
}

export default function LoginPage({ searchParams }: LoginPageProps) {
  const { redirectTo, message } = searchParams;

  return (
    <div className="min-h-screen flex items-center justify-center px-4 py-12 bg-gradient-to-br from-background-primary via-background-secondary to-background-primary">
      <div className="w-full max-w-md space-y-6">
        {/* 성공 메시지 표시 */}
        {message === "signup-success" && (
          <div className="p-4 bg-green-50 border border-green-200 rounded-md">
            <p className="text-sm text-green-700 text-center">
              회원가입이 완료되었습니다! 로그인해주세요.
            </p>
          </div>
        )}

        {/* 로그인 폼 */}
        <Suspense fallback={<LoadingSpinner size="lg" />}>
          <LoginForm redirectTo={redirectTo} />
        </Suspense>
      </div>
    </div>
  );
}
