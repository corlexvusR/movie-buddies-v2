/**
 * 사이드바 컴포넌트
 */

"use client";

import { useState, useEffect } from "react";
import { X, ChevronLeft, ChevronRight } from "lucide-react";
import { Button } from "@/components/ui/Button";
import { Navigation, navigationItems } from "./Navigation";
import { useUIStore } from "@/store/uiStore";
import { useAuth } from "@/hooks/useAuth";
import { cn } from "@/lib/utils";

interface SidebarProps {
  className?: string;
}

export function Sidebar({ className }: SidebarProps) {
  const { sidebarOpen, setSidebarOpen, mobileNavOpen, setMobileNavOpen } =
    useUIStore();
  const { isAuthenticated, user } = useAuth();
  const [isMobile, setIsMobile] = useState(false);

  // 모바일 화면 감지
  useEffect(() => {
    const checkMobile = () => {
      setIsMobile(window.innerWidth < 768);
    };

    checkMobile();
    window.addEventListener("resize", checkMobile);
    return () => window.removeEventListener("resize", checkMobile);
  }, []);

  // 모바일에서는 오버레이로 표시
  if (isMobile) {
    return (
      <>
        {/* 모바일 오버레이 */}
        {mobileNavOpen && (
          <div
            className="fixed inset-0 z-40 bg-black/50 md:hidden"
            onClick={() => setMobileNavOpen(false)}
          />
        )}

        {/* 모바일 사이드바 */}
        <aside
          className={cn(
            "fixed left-0 top-0 z-50 h-full w-64 bg-background-primary border-r border-border-primary transform transition-transform duration-200 md:hidden",
            mobileNavOpen ? "translate-x-0" : "-translate-x-full",
            className
          )}
        >
          <div className="flex items-center justify-between p-4 border-b border-border-primary">
            <h2 className="text-lg font-semibold text-text-primary">메뉴</h2>
            <Button
              variant="ghost"
              size="icon"
              onClick={() => setMobileNavOpen(false)}
            >
              <X className="h-5 w-5" />
            </Button>
          </div>

          <div className="p-4">
            <Navigation
              items={navigationItems}
              orientation="vertical"
              showIcons={true}
            />
          </div>
        </aside>
      </>
    );
  }

  // 데스크톱 사이드바
  return (
    <aside
      className={cn(
        "hidden md:flex flex-col bg-background-primary border-r border-border-primary transition-all duration-200",
        sidebarOpen ? "w-64" : "w-16",
        className
      )}
    >
      {/* 헤더 */}
      <div className="flex items-center justify-between p-4 border-b border-border-primary">
        {sidebarOpen && (
          <h2 className="text-lg font-semibold text-text-primary truncate">
            MovieBuddies
          </h2>
        )}
        <Button
          variant="ghost"
          size="icon"
          onClick={() => setSidebarOpen(!sidebarOpen)}
          className="flex-shrink-0"
        >
          {sidebarOpen ? (
            <ChevronLeft className="h-5 w-5" />
          ) : (
            <ChevronRight className="h-5 w-5" />
          )}
        </Button>
      </div>

      {/* 네비게이션 */}
      <div className="flex-1 p-4">
        <Navigation
          items={navigationItems.map((item) => ({
            ...item,
            name: sidebarOpen ? item.name : "",
          }))}
          orientation="vertical"
          showIcons={true}
          className={!sidebarOpen ? "items-center" : ""}
        />
      </div>

      {/* 사용자 정보 */}
      {isAuthenticated && user && (
        <div className="border-t border-border-primary p-4">
          <div className="flex items-center space-x-3">
            <div className="w-8 h-8 bg-accent-primary rounded-full flex items-center justify-center text-accent-primary-foreground text-sm font-medium">
              {user.nickname.charAt(0)}
            </div>
            {sidebarOpen && (
              <div className="flex-1 min-w-0">
                <p className="text-sm font-medium text-text-primary truncate">
                  {user.nickname}
                </p>
                <p className="text-xs text-text-muted truncate">
                  @{user.username}
                </p>
              </div>
            )}
          </div>
        </div>
      )}
    </aside>
  );
}
