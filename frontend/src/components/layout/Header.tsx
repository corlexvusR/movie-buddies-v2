/**
 * 헤더 컴포넌트
 * 네비게이션, 검색, 사용자 메뉴를 포함한 전체 헤더 구조
 */

"use client";

import Link from "next/link";
import { Menu, X, User, LogOut, Settings, Lock, Users } from "lucide-react";
import { Button } from "@/components/ui/Button";
import { useAuth } from "@/hooks/useAuth";
import { useUIStore } from "@/store/uiStore";
import { Navigation, navigationItems } from "./Navigation";
import { SearchBar } from "@/components/common/SearchBar";
import { useState, useRef, useEffect } from "react";
import { ImageWithFallback } from "@/components/common/ImageWithFallback";

export function Header() {
  const { isAuthenticated, user, logout, isInitialized } = useAuth();
  const { mobileNavOpen, toggleMobileNav, setMobileNavOpen } = useUIStore();
  const [profileDropdownOpen, setProfileDropdownOpen] = useState(false);
  const dropdownRef = useRef<HTMLDivElement>(null);

  // 프로필 드롭다운 외부 클릭 감지
  useEffect(() => {
    const handleClickOutside = (event: MouseEvent) => {
      if (
        dropdownRef.current &&
        !dropdownRef.current.contains(event.target as Node)
      ) {
        setProfileDropdownOpen(false);
      }
    };

    document.addEventListener("mousedown", handleClickOutside);
    return () => document.removeEventListener("mousedown", handleClickOutside);
  }, []);

  const handleLogout = () => {
    logout();
    setMobileNavOpen(false);
    setProfileDropdownOpen(false);
  };

  return (
    <header className="sticky top-0 z-40 bg-background-primary border-b border-border-primary">
      <div className="container mx-auto px-4">
        <div className="flex items-center justify-between h-16">
          {/* 로고 */}
          <Link
            href="/"
            className="text-xl font-bold text-text-primary hover:text-accent-primary transition-colors"
          >
            MovieBuddies
          </Link>

          {/* 데스크톱 네비게이션 */}
          <div className="hidden md:flex">
            <Navigation items={navigationItems} />
          </div>

          {/* 검색창 (데스크톱) */}
          <div className="hidden md:flex">
            <SearchBar className="w-64" />
          </div>

          {/* 사용자 메뉴 (데스크톱) */}
          <div className="hidden md:flex items-center space-x-4">
            {!isInitialized ? (
              // 로딩 시에도 일정한 크기 유지
              <div className="w-8 h-8 bg-background-secondary rounded-full"></div>
            ) : isAuthenticated ? (
              <div className="relative" ref={dropdownRef}>
                {/* 프로필 아바타 버튼 */}
                <button
                  onClick={() => setProfileDropdownOpen(!profileDropdownOpen)}
                  className="flex items-center space-x-2 p-1 rounded-full hover:bg-background-secondary transition-colors"
                >
                  <ImageWithFallback
                    src={user?.profileImageUrl || ""}
                    alt={user?.nickname || ""}
                    width={32}
                    height={32}
                    className="rounded-full"
                    fallbackType="profile"
                    iconSize={20}
                  />
                </button>

                {/* 드롭다운 메뉴 */}
                {profileDropdownOpen && (
                  <div className="absolute right-0 mt-2 w-56 bg-background-primary border border-border-primary rounded-lg shadow-lg py-2 z-50">
                    {/* 사용자 정보 */}
                    <div className="px-4 py-3 border-b border-border-primary">
                      <div className="flex items-center space-x-3">
                        <ImageWithFallback
                          src={user?.profileImageUrl || ""}
                          alt={user?.nickname || ""}
                          width={40}
                          height={40}
                          className="rounded-full"
                          fallbackType="profile"
                          iconSize={24}
                        />
                        <div className="flex-1 min-w-0">
                          <p className="text-sm font-medium text-text-primary truncate">
                            {user?.nickname}
                          </p>
                          <p className="text-xs text-text-muted truncate">
                            @{user?.username}
                          </p>
                        </div>
                      </div>
                    </div>

                    {/* 메뉴 항목들 */}
                    <div className="py-1">
                      <Link
                        href={`/profile/${user?.username}`}
                        onClick={() => setMobileNavOpen(false)}
                        className="flex items-center px-3 py-2 text-base font-medium text-text-muted hover:text-accent-primary hover:bg-background-secondary rounded-md transition-colors"
                      >
                        <User className="h-5 w-5 mr-3" />내 프로필
                      </Link>

                      <Link
                        href="/profile/edit"
                        onClick={() => setMobileNavOpen(false)}
                        className="flex items-center px-3 py-2 text-base font-medium text-text-muted hover:text-accent-primary hover:bg-background-secondary rounded-md transition-colors"
                      >
                        <Settings className="h-5 w-5 mr-3" />
                        프로필 수정
                      </Link>

                      <Link
                        href="/profile/settings/password"
                        onClick={() => setMobileNavOpen(false)}
                        className="flex items-center px-3 py-2 text-base font-medium text-text-muted hover:text-accent-primary hover:bg-background-secondary rounded-md transition-colors"
                      >
                        <Lock className="h-5 w-5 mr-3" />
                        비밀번호 변경
                      </Link>

                      <Link
                        href="/friends"
                        onClick={() => setMobileNavOpen(false)}
                        className="flex items-center px-3 py-2 text-base font-medium text-text-muted hover:text-accent-primary hover:bg-background-secondary rounded-md transition-colors"
                      >
                        <Users className="h-5 w-5 mr-3" />
                        친구 관리
                      </Link>
                    </div>

                    {/* 구분선 및 로그아웃 */}
                    <div className="border-t border-border-primary pt-1">
                      <button
                        onClick={handleLogout}
                        className="flex items-center w-full px-4 py-2 text-sm text-text-primary hover:bg-background-secondary transition-colors"
                      >
                        <LogOut className="h-4 w-4 mr-3" />
                        로그아웃
                      </button>
                    </div>
                  </div>
                )}
              </div>
            ) : (
              <div className="flex items-center space-x-2">
                <Link href="/auth/login">
                  <Button variant="outline" size="sm">
                    로그인
                  </Button>
                </Link>
                <Link href="/auth/signup">
                  <Button variant="primary" size="sm">
                    회원가입
                  </Button>
                </Link>
              </div>
            )}
          </div>

          {/* 모바일 메뉴 버튼 */}
          <Button
            variant="ghost"
            size="icon"
            className="md:hidden"
            onClick={toggleMobileNav}
          >
            {mobileNavOpen ? (
              <X className="h-5 w-5" />
            ) : (
              <Menu className="h-5 w-5" />
            )}
          </Button>
        </div>

        {/* 모바일 메뉴 */}
        {mobileNavOpen && (
          <div className="md:hidden border-t border-border-primary">
            <div className="px-2 pt-2 pb-3 space-y-1">
              {/* 모바일 검색 */}
              <div className="px-3 py-2">
                <SearchBar />
              </div>

              {/* 모바일 네비게이션 */}
              <Navigation
                items={navigationItems}
                orientation="vertical"
                showIcons={true}
                className="px-3"
              />

              {/* 모바일 사용자 메뉴 */}
              {!isInitialized ? (
                <div className="border-t border-border-primary pt-4 mt-4">
                  <div className="flex items-center px-3 py-2">
                    <div className="w-10 h-10 bg-background-secondary rounded-full mr-3"></div>
                    <div className="h-4 w-20 bg-background-secondary rounded"></div>
                  </div>
                </div>
              ) : isAuthenticated ? (
                <div className="border-t border-border-primary pt-4 mt-4">
                  {/* 모바일 사용자 정보 */}
                  <div className="flex items-center px-3 py-2 mb-2">
                    <ImageWithFallback
                      src={user?.profileImageUrl || ""}
                      alt={user?.nickname || ""}
                      width={40}
                      height={40}
                      className="rounded-full mr-3"
                      fallbackType="profile"
                      iconSize={24}
                    />
                    <div className="flex-1 min-w-0">
                      <p className="text-sm font-medium text-text-primary truncate">
                        {user?.nickname}
                      </p>
                      <p className="text-xs text-text-muted truncate">
                        @{user?.username}
                      </p>
                    </div>
                  </div>

                  {/* 모바일 메뉴 항목들 */}
                  <Link
                    href={`/profile/${user?.username}`}
                    onClick={() => setMobileNavOpen(false)}
                    className="flex items-center px-3 py-2 text-base font-medium text-text-muted hover:text-accent-primary hover:bg-background-secondary rounded-md transition-colors"
                  >
                    <User className="h-5 w-5 mr-3" />내 프로필
                  </Link>

                  <Link
                    href="/profile/edit"
                    onClick={() => setMobileNavOpen(false)}
                    className="flex items-center px-3 py-2 text-base font-medium text-text-muted hover:text-accent-primary hover:bg-background-secondary rounded-md transition-colors"
                  >
                    <Settings className="h-5 w-5 mr-3" />
                    프로필 관리
                  </Link>

                  <button
                    onClick={handleLogout}
                    className="flex items-center w-full px-3 py-2 text-base font-medium text-text-muted hover:text-accent-primary hover:bg-background-secondary rounded-md transition-colors"
                  >
                    <LogOut className="h-5 w-5 mr-3" />
                    로그아웃
                  </button>
                </div>
              ) : (
                <div className="border-t border-border-primary pt-4 mt-4 space-y-2">
                  <Link
                    href="/auth/login"
                    onClick={() => setMobileNavOpen(false)}
                    className="block px-3 py-2 text-base font-medium text-text-muted hover:text-accent-primary hover:bg-background-secondary rounded-md transition-colors"
                  >
                    로그인
                  </Link>
                  <Link
                    href="/auth/signup"
                    onClick={() => setMobileNavOpen(false)}
                    className="block px-3 py-2 text-base font-medium text-accent-primary bg-accent-primary/10 hover:bg-accent-primary/20 rounded-md transition-colors"
                  >
                    회원가입
                  </Link>
                </div>
              )}
            </div>
          </div>
        )}
      </div>
    </header>
  );
}
