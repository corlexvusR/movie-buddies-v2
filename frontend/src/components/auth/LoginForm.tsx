"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
import Link from "next/link";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
import { Eye, EyeOff } from "lucide-react";
import { Button } from "@/components/ui/Button";
import { Input } from "@/components/ui/Input";
import { Card } from "@/components/ui/Card";
import { useAuth } from "@/hooks/useAuth";
import { cn } from "@/lib/utils";

const loginSchema = z.object({
  username: z
    .string()
    .min(1, "사용자명을 입력해주세요")
    .min(3, "사용자명은 최소 3자 이상이어야 합니다"),
  password: z
    .string()
    .min(1, "비밀번호를 입력해주세요")
    .min(6, "비밀번호는 최소 6자 이상이어야 합니다"),
});

type LoginFormData = z.infer<typeof loginSchema>;

interface LoginFormProps {
  redirectTo?: string;
  className?: string;
}

export function LoginForm({ redirectTo = "/", className }: LoginFormProps) {
  const [showPassword, setShowPassword] = useState(false);
  const router = useRouter();
  const { login, isLoading } = useAuth();

  const {
    register,
    handleSubmit,
    formState: { errors },
    setError,
  } = useForm<LoginFormData>({
    resolver: zodResolver(loginSchema),
  });

  const onSubmit = async (data: LoginFormData) => {
    try {
      await login(data);
      router.push(redirectTo);
    } catch (error: unknown) {
      const axiosError = error as { response?: { status: number } };
      if (axiosError.response?.status === 401) {
        setError("root", {
          message: "사용자명 또는 비밀번호가 올바르지 않습니다.",
        });
      } else {
        const errorMessage =
          error instanceof Error
            ? error.message
            : "로그인 중 오류가 발생했습니다.";
        setError("root", { message: errorMessage });
      }
    }
  };

  return (
    <Card className={cn("w-full max-w-md p-6", className)}>
      <div className="space-y-6">
        {/* 헤더 */}
        <div className="text-center space-y-2">
          <h1 className="text-2xl font-bold text-text-primary">로그인</h1>
        </div>

        {/* 로그인 폼 */}
        <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
          {/* 사용자명 */}
          <div className="space-y-1">
            <label
              htmlFor="username"
              className="text-sm font-medium text-text-primary"
            >
              아이디
            </label>
            <Input
              id="username"
              type="text"
              placeholder="아이디를 입력하세요"
              {...register("username")}
              error={errors.username?.message}
              disabled={isLoading}
            />
            {errors.username && (
              <p className="text-sm text-red-500">{errors.username.message}</p>
            )}
          </div>

          {/* 비밀번호 */}
          <div className="space-y-1">
            <label
              htmlFor="password"
              className="text-sm font-medium text-text-primary"
            >
              비밀번호
            </label>
            <div className="relative">
              <Input
                id="password"
                type={showPassword ? "text" : "password"}
                placeholder="비밀번호를 입력하세요"
                {...register("password")}
                error={errors.password?.message}
                disabled={isLoading}
                className="pr-10"
              />
              <button
                type="button"
                onClick={() => setShowPassword(!showPassword)}
                className="absolute right-3 top-1/2 -translate-y-1/2 text-text-muted hover:text-text-primary"
                disabled={isLoading}
              >
                {showPassword ? (
                  <EyeOff className="h-4 w-4" />
                ) : (
                  <Eye className="h-4 w-4" />
                )}
              </button>
            </div>
            {errors.password && (
              <p className="text-sm text-red-500">{errors.password.message}</p>
            )}
          </div>

          {/* 전체 에러 메시지 */}
          {errors.root && (
            <div className="p-3 bg-red-50 border border-red-200 rounded-md">
              <p className="text-sm text-red-600">{errors.root.message}</p>
            </div>
          )}

          {/* 로그인 버튼 */}
          <Button type="submit" className="w-full" disabled={isLoading}>
            {isLoading ? "로그인 중..." : "로그인"}
          </Button>
        </form>

        {/* 추가 링크 */}
        <div className="space-y-4">
          <div className="text-center">
            <p className="text-sm text-text-muted">
              계정이 없으신가요?{" "}
              <Link
                href="/auth/signup"
                className="text-accent-primary hover:text-accent-primary/80 font-medium"
              >
                회원가입
              </Link>
            </p>
          </div>

          {/* 구분선 */}
          <div className="relative">
            <div className="absolute inset-0 flex items-center">
              <div className="w-full border-t border-border-primary" />
            </div>
            <div className="relative flex justify-center text-sm">
              <span className="px-2 bg-background-primary text-text-muted">
                또는
              </span>
            </div>
          </div>

          {/* 홈으로 가기 */}
          <div className="text-center">
            <Link
              href="/"
              className="text-sm text-text-muted hover:text-text-primary"
            >
              홈페이지로 돌아가기
            </Link>
          </div>
        </div>
      </div>
    </Card>
  );
}
