"use client";

import React, { useState } from "react";
import { useRouter } from "next/navigation";
import Link from "next/link";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { Eye, EyeOff, Check, X } from "lucide-react";
import { Button } from "@/components/ui/Button";
import { Input } from "@/components/ui/Input";
import { Card } from "@/components/ui/Card";
import { useAuth } from "@/hooks/useAuth";
import { SignupRequest } from "@/types/auth";
import { useDebounce } from "@/hooks/useDebounce";
import { apiClient } from "@/lib/api";
import { API_ENDPOINTS } from "@/lib/constants";
import { signupFormSchema, type SignupFormWithConfirm } from "@/lib/validation";

interface ValidationStatus {
  username: "idle" | "checking" | "available" | "unavailable";
  email: "idle" | "checking" | "available" | "unavailable";
  nickname: "idle" | "checking" | "available" | "unavailable";
}

export function SignupForm() {
  const [showPassword, setShowPassword] = useState(false);
  const [showPasswordConfirm, setShowPasswordConfirm] = useState(false);
  const [validationStatus, setValidationStatus] = useState<ValidationStatus>({
    username: "idle",
    email: "idle",
    nickname: "idle",
  });

  const router = useRouter();
  const { signup, isLoading } = useAuth();

  const {
    register,
    handleSubmit,
    watch,
    formState: { errors },
    setError,
  } = useForm<SignupFormWithConfirm>({
    resolver: zodResolver(signupFormSchema),
  });

  const watchedUsername = watch("username");
  const watchedEmail = watch("email");
  const watchedNickname = watch("nickname");

  const debouncedUsername = useDebounce(watchedUsername, 500);
  const debouncedEmail = useDebounce(watchedEmail, 500);
  const debouncedNickname = useDebounce(watchedNickname, 500);

  // 중복 확인 함수
  const checkAvailability = async (
    type: "username" | "email" | "nickname",
    value: string
  ) => {
    if (!value || value.length < 2) return;

    setValidationStatus((prev) => ({ ...prev, [type]: "checking" }));

    try {
      const endpoint =
        type === "username"
          ? API_ENDPOINTS.AUTH.CHECK_USERNAME
          : type === "email"
          ? API_ENDPOINTS.AUTH.CHECK_EMAIL
          : API_ENDPOINTS.AUTH.CHECK_NICKNAME;

      const response = await apiClient.get<boolean>(
        `${endpoint}?${type}=${encodeURIComponent(value)}`
      );

      setValidationStatus((prev) => ({
        ...prev,
        [type]: response ? "available" : "unavailable",
      }));
    } catch (error: unknown) {
      console.error("Signup failed:", error);
      setValidationStatus((prev) => ({ ...prev, [type]: "unavailable" }));
    }
  };

  // 디바운스된 값 변경 시 중복 확인
  React.useEffect(() => {
    if (debouncedUsername && debouncedUsername.length >= 3) {
      checkAvailability("username", debouncedUsername);
    }
  }, [debouncedUsername]);

  React.useEffect(() => {
    if (debouncedEmail && debouncedEmail.includes("@")) {
      checkAvailability("email", debouncedEmail);
    }
  }, [debouncedEmail]);

  React.useEffect(() => {
    if (debouncedNickname && debouncedNickname.length >= 2) {
      checkAvailability("nickname", debouncedNickname);
    }
  }, [debouncedNickname]);

  const onSubmit = async (data: SignupFormWithConfirm) => {
    // 중복 확인 상태 체크
    if (validationStatus.username === "unavailable") {
      setError("username", { message: "이미 사용 중인 사용자명입니다" });
      return;
    }
    if (validationStatus.email === "unavailable") {
      setError("email", { message: "이미 사용 중인 이메일입니다" });
      return;
    }
    if (validationStatus.nickname === "unavailable") {
      setError("nickname", { message: "이미 사용 중인 닉네임입니다" });
      return;
    }

    try {
      // 백엔드 SignupRequest 형태로 변환
      const signupData: SignupRequest = {
        username: data.username,
        email: data.email,
        nickname: data.nickname,
        password: data.password, // 단일 password 사용
      };

      await signup(signupData);
      router.push("/auth/login?message=signup-success");
    } catch (error: unknown) {
      const axiosError = error as {
        response?: {
          data?: { errors?: Record<string, string[]> };
          status?: number;
        };
      };

      if (axiosError.response?.data?.errors) {
        const serverErrors = axiosError.response.data.errors;
        Object.entries(serverErrors).forEach(([field, messages]) => {
          if (Array.isArray(messages) && messages.length > 0) {
            setError(field as keyof SignupFormWithConfirm, {
              message: messages[0],
            });
          }
        });
      } else {
        const errorMessage =
          error instanceof Error
            ? error.message
            : "회원가입 중 오류가 발생했습니다.";
        setError("root", { message: errorMessage });
      }
    }
  };

  const ValidationIcon = ({
    status,
  }: {
    status: ValidationStatus[keyof ValidationStatus];
  }) => {
    switch (status) {
      case "checking":
        return (
          <div className="animate-spin h-4 w-4 border-2 border-accent-primary border-t-transparent rounded-full" />
        );
      case "available":
        return <Check className="h-4 w-4 text-green-500" />;
      case "unavailable":
        return <X className="h-4 w-4 text-red-500" />;
      default:
        return null;
    }
  };

  return (
    <Card className="w-full max-w-md p-6">
      <div className="space-y-6">
        <div className="text-center space-y-2">
          <h1 className="text-2xl font-bold text-text-primary">회원가입</h1>
          <p className="text-text-muted">
            MovieBuddies 계정을 만들어 영화 정보를 공유해보세요
          </p>
        </div>

        <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
          {/* 사용자명 */}
          <div className="space-y-1">
            <label
              htmlFor="username"
              className="text-sm font-medium text-text-primary"
            >
              사용자명
            </label>
            <div className="relative">
              <Input
                id="username"
                type="text"
                placeholder="영문, 숫자, _ 사용 가능"
                {...register("username")}
                error={
                  errors.username?.message ||
                  (validationStatus.username === "unavailable"
                    ? "이미 사용 중인 사용자명입니다"
                    : undefined)
                }
                disabled={isLoading}
                className="pr-10"
              />
              <div className="absolute right-3 top-1/2 -translate-y-1/2">
                <ValidationIcon status={validationStatus.username} />
              </div>
            </div>
            {validationStatus.username === "available" && !errors.username && (
              <p className="text-sm text-green-500">
                사용 가능한 사용자명입니다
              </p>
            )}
          </div>

          {/* 이메일 */}
          <div className="space-y-1">
            <label
              htmlFor="email"
              className="text-sm font-medium text-text-primary"
            >
              이메일
            </label>
            <div className="relative">
              <Input
                id="email"
                type="email"
                placeholder="example@email.com"
                {...register("email")}
                error={
                  errors.email?.message ||
                  (validationStatus.email === "unavailable"
                    ? "이미 사용 중인 이메일입니다"
                    : undefined)
                }
                disabled={isLoading}
                className="pr-10"
              />
              <div className="absolute right-3 top-1/2 -translate-y-1/2">
                <ValidationIcon status={validationStatus.email} />
              </div>
            </div>
            {validationStatus.email === "available" && !errors.email && (
              <p className="text-sm text-green-500">사용 가능한 이메일입니다</p>
            )}
          </div>

          {/* 닉네임 */}
          <div className="space-y-1">
            <label
              htmlFor="nickname"
              className="text-sm font-medium text-text-primary"
            >
              닉네임
            </label>
            <div className="relative">
              <Input
                id="nickname"
                type="text"
                placeholder="다른 사용자에게 표시될 이름"
                {...register("nickname")}
                error={
                  errors.nickname?.message ||
                  (validationStatus.nickname === "unavailable"
                    ? "이미 사용 중인 닉네임입니다"
                    : undefined)
                }
                disabled={isLoading}
                className="pr-10"
              />
              <div className="absolute right-3 top-1/2 -translate-y-1/2">
                <ValidationIcon status={validationStatus.nickname} />
              </div>
            </div>
            {validationStatus.nickname === "available" && !errors.nickname && (
              <p className="text-sm text-green-500">사용 가능한 닉네임입니다</p>
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
                placeholder="8자 이상, 영문, 숫자 포함"
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
          </div>

          {/* 비밀번호 확인 */}
          <div className="space-y-1">
            <label
              htmlFor="confirmPassword"
              className="text-sm font-medium text-text-primary"
            >
              비밀번호 확인
            </label>
            <div className="relative">
              <Input
                id="confirmPassword"
                type={showPasswordConfirm ? "text" : "password"}
                placeholder="비밀번호를 다시 입력하세요"
                {...register("confirmPassword")}
                error={errors.confirmPassword?.message}
                disabled={isLoading}
                className="pr-10"
              />
              <button
                type="button"
                onClick={() => setShowPasswordConfirm(!showPasswordConfirm)}
                className="absolute right-3 top-1/2 -translate-y-1/2 text-text-muted hover:text-text-primary"
                disabled={isLoading}
              >
                {showPasswordConfirm ? (
                  <EyeOff className="h-4 w-4" />
                ) : (
                  <Eye className="h-4 w-4" />
                )}
              </button>
            </div>
          </div>

          {/* 전체 에러 메시지 */}
          {errors.root && (
            <div className="p-3 bg-red-50 border border-red-200 rounded-md">
              <p className="text-sm text-red-600">{errors.root.message}</p>
            </div>
          )}

          {/* 회원가입 버튼 */}
          <Button type="submit" className="w-full" loading={isLoading}>
            계정 만들기
          </Button>
        </form>

        <div className="space-y-4">
          <div className="text-center">
            <p className="text-sm text-text-muted">
              이미 계정이 있으신가요?{" "}
              <Link
                href="/auth/login"
                className="text-accent-primary hover:text-accent-primary/80 font-medium"
              >
                로그인
              </Link>
            </p>
          </div>

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
