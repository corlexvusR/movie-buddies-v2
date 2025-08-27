"use client";

import { useState, useRef, useEffect, useCallback } from "react";
import { useRouter } from "next/navigation";
import { Search, X, Clock } from "lucide-react";
import { Input } from "@/components/ui/Input";
import { Button } from "@/components/ui/Button";
import { Card } from "@/components/ui/Card";
import { useDebounce } from "@/hooks/useDebounce";
import { apiClient } from "@/lib/api";
import { storage } from "@/lib/utils";
import { cn } from "@/lib/utils";

interface SearchResult {
  id: number;
  title: string;
  type: "movie" | "actor";
  year?: number;
  posterPath?: string;
}

interface SearchBarProps {
  placeholder?: string;
  showHistory?: boolean;
  showSuggestions?: boolean;
  autoFocus?: boolean;
  className?: string;
  onSearch?: (query: string) => void;
}

interface ApiResponse {
  data: SearchResult[];
}

const SEARCH_HISTORY_KEY = "moviebuddies_search_history";
const MAX_HISTORY_ITEMS = 10;

export function SearchBar({
  placeholder = "영화, 배우 검색",
  showHistory = true,
  showSuggestions = true,
  autoFocus = false,
  className,
  onSearch,
}: SearchBarProps) {
  const router = useRouter();
  const [query, setQuery] = useState("");
  const [isOpen, setIsOpen] = useState(false);
  const [results, setResults] = useState<SearchResult[]>([]);
  const [history, setHistory] = useState<string[]>([]);
  const [isLoading, setIsLoading] = useState(false);
  const [selectedIndex, setSelectedIndex] = useState(-1);

  const searchRef = useRef<HTMLDivElement>(null);
  const inputRef = useRef<HTMLInputElement>(null);
  const debouncedQuery = useDebounce(query, 300);

  // 검색 히스토리 로드
  useEffect(() => {
    if (showHistory) {
      const savedHistory = storage.get<string[]>(SEARCH_HISTORY_KEY) || [];
      setHistory(savedHistory);
    }
  }, [showHistory]);

  // 검색 실행
  const performSearch = useCallback(async (searchQuery: string) => {
    try {
      setIsLoading(true);
      const response = (await apiClient.get("/search/suggestions", {
        q: searchQuery,
        limit: 8,
      })) as ApiResponse;
      setResults(response.data || []);
    } catch (error) {
      console.error("Search failed:", error);
      setResults([]);
    } finally {
      setIsLoading(false);
    }
  }, []);

  useEffect(() => {
    if (debouncedQuery.trim() && showSuggestions) {
      performSearch(debouncedQuery);
    } else {
      setResults([]);
      setIsLoading(false);
    }
  }, [debouncedQuery, showSuggestions, performSearch]);

  const handleSearch = useCallback(
    (searchQuery: string) => {
      const trimmedQuery = searchQuery.trim();
      if (!trimmedQuery) return;

      // 히스토리에 추가
      if (showHistory) {
        const newHistory = [
          trimmedQuery,
          ...history.filter((item) => item !== trimmedQuery),
        ].slice(0, MAX_HISTORY_ITEMS);

        setHistory(newHistory);
        storage.set(SEARCH_HISTORY_KEY, newHistory);
      }

      // 검색 실행
      setIsOpen(false);
      setSelectedIndex(-1);

      if (onSearch) {
        onSearch(trimmedQuery);
      } else {
        router.push(`/search?q=${encodeURIComponent(trimmedQuery)}`);
      }
    },
    [history, showHistory, onSearch, router]
  );

  const handleItemSelect = useCallback(
    (index: number) => {
      const historyLength = showHistory ? history.length : 0;

      if (index < historyLength) {
        // 히스토리 아이템 선택
        const selectedHistory = history[index];
        setQuery(selectedHistory);
        handleSearch(selectedHistory);
      } else {
        // 검색 결과 아이템 선택
        const result = results[index - historyLength];
        if (result) {
          const searchTerm = result.title;
          setQuery(searchTerm);

          // 타입별 페이지로 이동
          switch (result.type) {
            case "movie":
              router.push(`/movies/${result.id}`);
              break;
            case "actor":
              router.push(`/actors/${result.id}`);
              break;
          }

          setIsOpen(false);
          setSelectedIndex(-1);
        }
      }
    },
    [history, results, showHistory, handleSearch, router]
  );

  // 외부 클릭 감지
  useEffect(() => {
    const handleClickOutside = (event: MouseEvent) => {
      if (
        searchRef.current &&
        !searchRef.current.contains(event.target as Node)
      ) {
        setIsOpen(false);
        setSelectedIndex(-1);
      }
    };

    document.addEventListener("mousedown", handleClickOutside);
    return () => document.removeEventListener("mousedown", handleClickOutside);
  }, []);

  // 키보드 네비게이션
  useEffect(() => {
    const handleKeyDown = (event: KeyboardEvent) => {
      if (!isOpen) return;

      const totalItems = results.length + (showHistory ? history.length : 0);

      switch (event.key) {
        case "ArrowDown":
          event.preventDefault();
          setSelectedIndex((prev) => (prev + 1) % totalItems);
          break;
        case "ArrowUp":
          event.preventDefault();
          setSelectedIndex((prev) => (prev <= 0 ? totalItems - 1 : prev - 1));
          break;
        case "Enter":
          event.preventDefault();
          if (selectedIndex >= 0) {
            handleItemSelect(selectedIndex);
          } else if (query.trim()) {
            handleSearch(query);
          }
          break;
        case "Escape":
          setIsOpen(false);
          setSelectedIndex(-1);
          inputRef.current?.blur();
          break;
      }
    };

    document.addEventListener("keydown", handleKeyDown);
    return () => document.removeEventListener("keydown", handleKeyDown);
  }, [
    isOpen,
    selectedIndex,
    results,
    history,
    query,
    showHistory,
    handleItemSelect,
    handleSearch,
  ]);

  const removeFromHistory = (item: string, event: React.MouseEvent) => {
    event.stopPropagation();
    const newHistory = history.filter((h) => h !== item);
    setHistory(newHistory);
    storage.set(SEARCH_HISTORY_KEY, newHistory);
  };

  const clearHistory = () => {
    setHistory([]);
    storage.remove(SEARCH_HISTORY_KEY);
  };

  const getItemIcon = (type: string) => {
    switch (type) {
      case "movie":
        return "🎬";
      case "actor":
        return "🎭";
      default:
        return "🔍";
    }
  };

  const shouldShowDropdown =
    isOpen &&
    ((showHistory && history.length > 0 && !query) ||
      (showSuggestions && (results.length > 0 || isLoading)));

  return (
    <div ref={searchRef} className={cn("relative w-full max-w-lg", className)}>
      {/* 검색 입력 */}
      <div className="relative">
        <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 w-4 h-4 text-text-muted" />
        <Input
          ref={inputRef}
          type="text"
          placeholder={placeholder}
          value={query}
          onChange={(e) => setQuery(e.target.value)}
          onFocus={() => setIsOpen(true)}
          onKeyDown={(e) => {
            if (e.key === "Enter") {
              e.preventDefault();
              handleSearch(query);
            }
          }}
          className="pl-10 pr-10"
          autoFocus={autoFocus}
        />

        {query && (
          <Button
            variant="ghost"
            size="sm"
            onClick={() => {
              setQuery("");
              setResults([]);
              inputRef.current?.focus();
            }}
            className="absolute right-2 top-1/2 transform -translate-y-1/2 h-6 w-6 p-0"
          >
            <X className="w-3 h-3" />
          </Button>
        )}
      </div>

      {/* 드롭다운 */}
      {shouldShowDropdown && (
        <Card className="absolute top-full left-0 right-0 mt-1 z-50 max-h-96 overflow-y-auto">
          <div className="py-2">
            {/* 검색 히스토리 */}
            {showHistory && history.length > 0 && !query && (
              <div>
                <div className="flex items-center justify-between px-3 py-2">
                  <span className="text-xs font-medium text-text-muted uppercase tracking-wider">
                    최근 검색
                  </span>
                  <Button
                    variant="ghost"
                    size="sm"
                    onClick={clearHistory}
                    className="text-xs text-text-muted hover:text-text-primary"
                  >
                    전체 삭제
                  </Button>
                </div>

                {history.map((item, index) => (
                  <button
                    key={item}
                    onClick={() => handleItemSelect(index)}
                    className={cn(
                      "w-full flex items-center justify-between px-3 py-2 text-left hover:bg-background-tertiary transition-colors group",
                      selectedIndex === index && "bg-background-tertiary"
                    )}
                  >
                    <div className="flex items-center space-x-3">
                      <Clock className="w-4 h-4 text-text-muted" />
                      <span className="text-sm text-text-primary">{item}</span>
                    </div>
                    <Button
                      variant="ghost"
                      size="sm"
                      onClick={(e) => removeFromHistory(item, e)}
                      className="h-6 w-6 p-0 opacity-0 group-hover:opacity-100"
                    >
                      <X className="w-3 h-3" />
                    </Button>
                  </button>
                ))}
              </div>
            )}

            {/* 검색 결과 */}
            {showSuggestions && (
              <div>
                {isLoading && (
                  <div className="px-3 py-4 text-center">
                    <div className="inline-block animate-spin rounded-full h-4 w-4 border-b-2 border-accent-primary"></div>
                    <span className="ml-2 text-sm text-text-muted">
                      검색 중
                    </span>
                  </div>
                )}

                {!isLoading && results.length > 0 && (
                  <div>
                    {query && (
                      <div className="px-3 py-2">
                        <span className="text-xs font-medium text-text-muted uppercase tracking-wider">
                          검색 결과
                        </span>
                      </div>
                    )}

                    {results.map((result, index) => {
                      const actualIndex =
                        (showHistory ? history.length : 0) + index;
                      return (
                        <button
                          key={`${result.type}-${result.id}`}
                          onClick={() => handleItemSelect(actualIndex)}
                          className={cn(
                            "w-full flex items-center space-x-3 px-3 py-2 text-left hover:bg-background-tertiary transition-colors",
                            selectedIndex === actualIndex &&
                              "bg-background-tertiary"
                          )}
                        >
                          <span className="text-lg">
                            {getItemIcon(result.type)}
                          </span>
                          <div className="flex-1 min-w-0">
                            <div className="text-sm font-medium text-text-primary truncate">
                              {result.title}
                            </div>
                            <div className="text-xs text-text-muted">
                              {result.type === "movie" &&
                                result.year &&
                                `${result.year} • `}
                              {result.type === "movie" ? "영화" : "배우"}
                            </div>
                          </div>
                        </button>
                      );
                    })}
                  </div>
                )}

                {!isLoading && results.length === 0 && query && (
                  <div className="px-3 py-4 text-center text-sm text-text-muted">
                    {query}에 대한 검색 결과가 없습니다.
                  </div>
                )}
              </div>
            )}
          </div>
        </Card>
      )}
    </div>
  );
}
