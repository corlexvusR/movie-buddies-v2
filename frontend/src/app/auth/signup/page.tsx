import { Suspense } from "react";
import { Metadata } from "next";
import { SignupForm } from "@/components/auth/SignupForm";
import { LoadingSpinner } from "@/components/ui/Loading";

export const metadata: Metadata = {
  title: "회원가입 - MovieBuddies",
  description:
    "MovieBuddies에 가입하여 영화 리뷰를 작성하고 친구들과 소통하세요!",
};

export default function SignupPage() {
  return (
    <div className="min-h-screen flex items-center justify-center px-4 py-12 bg-gradient-to-br from-background-primary via-background-secondary to-background-primary">
      <div className="w-full max-w-md">
        <Suspense fallback={<LoadingSpinner size="lg" />}>
          <SignupForm />
        </Suspense>
      </div>
    </div>
  );
}
