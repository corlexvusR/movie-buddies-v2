/**
 * 404 페이지 컴포넌트
 */
import Link from "next/link";
import { Button } from "@/components/ui/Button";
import { Home, Search } from "lucide-react";

export default function NotFound() {
  return (
    <div className="flex flex-col items-center justify-center min-h-[60vh] px-4">
      <div className="text-center space-y-6 max-w-md">
        <div className="space-y-2">
          <h1 className="text-6xl font-bold text-accent-primary">404</h1>
          <h2 className="text-2xl font-bold text-text-primary">
            페이지를 찾을 수 없습니다
          </h2>
          <p className="text-text-secondary">
            요청하신 페이지가 존재하지 않거나 이동되었습니다.
          </p>
        </div>

        <div className="flex gap-4 justify-center">
          <Link href="/">
            <Button variant="primary">
              <Home className="h-4 w-4 mr-2" />
              홈으로 돌아가기
            </Button>
          </Link>
          <Link href="/movies">
            <Button variant="outline">
              <Search className="h-4 w-4 mr-2" />
              영화 찾기
            </Button>
          </Link>
        </div>
      </div>
    </div>
  );
}
