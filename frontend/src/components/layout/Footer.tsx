/**
 * 푸터 컴포넌트
 */

import Link from "next/link";

export function Footer() {
  const currentYear = new Date().getFullYear();

  return (
    <footer className="bg-background-secondary border-t border-border-primary py-8">
      <div className="container mx-auto px-4 py-8">
        <div className="grid grid-cols-1 md:grid-cols-4 gap-8">
          {/* 브랜드 섹션 */}
          <div className="space-y-4">
            <h3 className="text-lg font-bold text-text-primary">
              MovieBuddies
            </h3>
            <p className="text-sm text-text-muted">
              영화 정보를 확인하고 친구들과 소통하는 <br></br> 소셜 플랫폼
            </p>
            <div className="flex space-x-4"></div>
          </div>

          {/* 링크 */}
          <div className="space-y-4">
            <h4 className="text-sm font-semibold text-text-primary">링크</h4>
            <ul className="space-y-2 text-sm">
              <li>
                <Link
                  href="/movies"
                  className="text-text-muted hover:text-accent-primary transition-colors"
                >
                  영화 목록
                </Link>
              </li>
              <li>
                <Link
                  href="/movies/genres"
                  className="text-text-muted hover:text-accent-primary transition-colors"
                >
                  장르별 영화
                </Link>
              </li>
              <li>
                <Link
                  href="/recommendations"
                  className="text-text-muted hover:text-accent-primary transition-colors"
                >
                  영화 추천
                </Link>
              </li>
              <li>
                <Link
                  href="/chat"
                  className="text-text-muted hover:text-accent-primary transition-colors"
                >
                  실시간 채팅
                </Link>
              </li>
            </ul>
          </div>

          {/* 고객 지원 */}
          <div className="space-y-4">
            <h4 className="text-sm font-semibold text-text-primary">
              고객 지원
            </h4>
            <ul className="space-y-2 text-sm">
              <li>
                <Link
                  href="/help"
                  className="text-text-muted hover:text-accent-primary transition-colors"
                >
                  도움말
                </Link>
              </li>
              <li>
                <Link
                  href="/contact"
                  className="text-text-muted hover:text-accent-primary transition-colors"
                >
                  문의하기
                </Link>
              </li>
              <li>
                <Link
                  href="/privacy"
                  className="text-text-muted hover:text-accent-primary transition-colors"
                >
                  개인정보처리방침
                </Link>
              </li>
              <li>
                <Link
                  href="/terms"
                  className="text-text-muted hover:text-accent-primary transition-colors"
                >
                  이용약관
                </Link>
              </li>
            </ul>
          </div>

          {/* 정보 */}
          <div className="space-y-4">
            <h4 className="text-sm font-semibold text-text-primary">정보</h4>
            <ul className="space-y-2 text-sm text-text-muted">
              <li>데이터 제공: TMDB</li>
              <li>버전: v2.0.0</li>
            </ul>
          </div>
        </div>

        {/* 하단 구분선 및 저작권 */}
        <div className="border-t border-border-primary mt-8 pt-6">
          <div className="flex flex-col md:flex-row justify-between items-center space-y-4 md:space-y-0">
            <p className="text-sm text-text-muted">
              © {currentYear} MovieBuddies. All rights reserved.
            </p>
            <p className="text-sm text-text-muted flex items-center"></p>
          </div>
        </div>
      </div>
    </footer>
  );
}
