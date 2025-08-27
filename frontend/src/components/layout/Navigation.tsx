/**
 * 네비게이션 컴포넌트
 */

"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import { Home, Film, Tag, MessageCircle, Star } from "lucide-react";
import { cn } from "@/lib/utils";
import { useMemo } from "react";

export interface NavigationItem {
  name: string;
  href: string;
  icon: React.ReactNode;
  badge?: string | number;
  external?: boolean;
}

interface NavigationProps {
  items: NavigationItem[];
  orientation?: "horizontal" | "vertical";
  showIcons?: boolean;
  className?: string;
}

export const navigationItems: NavigationItem[] = [
  { name: "홈", href: "/", icon: <Home className="w-5 h-5" /> },
  { name: "영화 목록", href: "/movies", icon: <Film className="w-5 h-5" /> },
  {
    name: "장르별 영화",
    href: "/movies/genre",
    icon: <Tag className="w-5 h-5" />,
  },
  {
    name: "영화 추천",
    href: "/recommendations",
    icon: <Star className="w-5 h-5" />,
  },
  {
    name: "실시간 채팅",
    href: "/chat",
    icon: <MessageCircle className="w-5 h-5" />,
  },
];

export function Navigation({
  items,
  orientation = "horizontal",
  showIcons = false,
  className,
}: NavigationProps) {
  const pathname = usePathname();

  // useMemo를 사용하는 이유:
  // 1. 성능 최적화: pathname이 변경될 때만 재계산
  // 2. 참조 안정성: 불필요한 리렌더링 방지
  // 3. 계산 비용: isActive 계산을 캐시하여 매번 계산하지 않음
  const itemsWithActiveState = useMemo(() => {
    return items.map((item) => {
      let isActive = false;

      if (item.href === "/") {
        // 홈페이지는 정확히 일치해야 함
        isActive = pathname === "/";
      } else {
        // 다른 페이지들의 활성 상태 계산
        // 더 구체적인 경로가 우선순위를 가짐
        const sortedItems = [...items].sort(
          (a, b) => b.href.length - a.href.length
        );
        const matchingItem = sortedItems.find(
          (sortedItem) =>
            sortedItem.href !== "/" && pathname.startsWith(sortedItem.href)
        );
        isActive = matchingItem?.href === item.href;
      }

      return {
        ...item,
        isActive,
      };
    });
  }, [items, pathname]);

  const baseClasses = cn(
    "flex transition-all duration-200",
    orientation === "horizontal" ? "space-x-8" : "flex-col space-y-1",
    className
  );

  const getLinkClasses = (isActive: boolean) =>
    cn(
      "text-sm font-medium transition-colors duration-200 rounded-md",
      "hover:text-accent-primary",
      isActive
        ? "text-accent-primary"
        : "text-text-muted hover:text-text-primary",
      orientation === "vertical" &&
        "block px-3 py-2 hover:bg-background-secondary"
    );

  return (
    <nav className={baseClasses}>
      {itemsWithActiveState.map((item) => {
        const content = (
          <span className="flex items-center space-x-2">
            {showIcons && <span className="flex-shrink-0">{item.icon}</span>}
            {item.name && <span>{item.name}</span>}
            {item.badge && (
              <span className="bg-accent-primary text-white text-xs px-2 py-1 rounded-full">
                {item.badge}
              </span>
            )}
          </span>
        );

        if (item.external) {
          return (
            <a
              key={item.name}
              href={item.href}
              target="_blank"
              rel="noopener noreferrer"
              className={getLinkClasses(false)}
            >
              {content}
            </a>
          );
        }

        return (
          <Link
            key={item.name}
            href={item.href}
            className={getLinkClasses(item.isActive)}
          >
            {content}
          </Link>
        );
      })}
    </nav>
  );
}
