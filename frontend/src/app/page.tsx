/**
 * 홈페이지 컴포넌트 (임시 버전)
 * 검증을 위해 home 컴포넌트들을 주석 처리하고 기본 구조만 표시
 */
import { Suspense } from "react";
import { Loading } from "@/components/ui/Loading";
import type { Metadata } from "next";

export const metadata: Metadata = {
  title: "MovieBuddies - 영화 정보 및 소셜 플랫폼",
  description: "최신 영화 정보를 확인하고 친구들과 소통해보세요",
};

export default function HomePage() {
  return (
    <div className="min-h-screen">
      {/* 히어로 섹션 */}
      <section className="bg-gradient-to-r from-accent-primary/10 to-accent-secondary/10 py-20">
        <div className="container mx-auto px-4 text-center">
          <h1 className="text-4xl md:text-6xl font-bold text-text-primary mb-6">
            MovieBuddies
          </h1>
          <p className="text-xl text-text-secondary mb-8 max-w-2xl mx-auto">
            최신 영화 정보를 확인하고 친구들과 소통하는 소셜 플랫폼
          </p>
          {/* <HeroSection /> */}
          <div className="text-text-muted">HeroSection 컴포넌트</div>
        </div>
      </section>

      <div className="container mx-auto px-4 py-8 space-y-12">
        {/* 인기 영화 섹션 */}

        <section>
          <h2 className="text-2xl font-bold text-text-primary mb-6">
            인기 영화
          </h2>
          <div className="bg-background-secondary rounded-lg p-8 text-center">
            {/* <Suspense fallback={<Loading />}>
              <PopularMovies />
            </Suspense> */}
            <Suspense fallback={<Loading />}>
              <p className="text-text-muted">PopularMovies 컴포넌트</p>
            </Suspense>
          </div>
        </section>

        {/* 추천 영화 섹션 (로그인 시에만 표시) */}
        <section>
          <h2 className="text-2xl font-bold text-text-primary mb-6">
            맞춤 추천 영화
          </h2>
          <div className="bg-background-secondary rounded-lg p-8 text-center">
            {/* <Suspense fallback={<Loading />}>
              <RecommendedMovies />
            </Suspense> */}
            <Suspense fallback={<Loading />}>
              <p className="text-text-muted">RecommendedMovies 컴포넌트</p>
            </Suspense>
          </div>
        </section>
      </div>
    </div>
  );
}
