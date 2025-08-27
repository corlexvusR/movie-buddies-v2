/**
 * 전역 로딩 페이지 컴포넌트
 * 페이지 전환 시 표시되는 로딩 UI
 */
import { Loading } from "@/components/ui/Loading";

export default function LoadingPage() {
  return (
    <div className="flex items-center justify-center min-h-[60vh]">
      <Loading size="lg" text="페이지를 불러오는 중" />
    </div>
  );
}
