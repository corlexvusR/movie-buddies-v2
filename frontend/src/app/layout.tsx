/**
 * 루트 레이아웃 컴포넌트
 * 전체 애플리케이션의 기본 구조와 메타데이터 설정
 */
import type { Metadata, Viewport } from "next";
import "./globals.css";
import { Providers } from "@/components/layout/Providers";
import { Header } from "@/components/layout/Header";
import { Footer } from "@/components/layout/Footer";

export const metadata: Metadata = {
  title: {
    default: "MovieBuddies - 영화 정보 및 소셜 플랫폼",
    template: "%s | MovieBuddies",
  },
  description: "영화 정보를 확인하고 친구들과 소통하는 소셜 플랫폼",
  keywords: ["영화", "리뷰", "추천", "소셜", "TMDB"],
  authors: [{ name: "MovieBuddies Team" }],
  creator: "MovieBuddies",
  openGraph: {
    type: "website",
    locale: "ko_KR",
    url: "https://moviebuddies.com",
    siteName: "MovieBuddies",
    title: "MovieBuddies - 영화 정보 및 소셜 플랫폼",
    description: "영화 정보를 확인하고 친구들과 소통하는 소셜 플랫폼",
  },
  twitter: {
    card: "summary_large_image",
    title: "MovieBuddies",
    description: "영화 정보를 확인하고 친구들과 소통하는 소셜 플랫폼",
  },
};

// viewport를 별도로 export
export const viewport: Viewport = {
  width: "device-width",
  initialScale: 1,
  maximumScale: 1,
};

export default function RootLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    <html lang="ko">
      <body className="min-h-screen bg-background-primary text-text-primary font-sans">
        <Providers>
          <div className="flex flex-col min-h-screen">
            <Header />
            <main className="flex-1 pb-8">{children}</main>
            <Footer />
          </div>
        </Providers>
      </body>
    </html>
  );
}
