import { type ClassValue, clsx } from "clsx";
import { twMerge } from "tailwind-merge";

/**
 * Tailwind CSS 클래스를 병합하는 유틸리티 함수
 * clsx와 tailwind-merge를 조합하여 클래스 충돌을 방지
 */
export function cn(...inputs: ClassValue[]) {
  return twMerge(clsx(inputs));
}

/**
 * 숫자를 한국어 형식으로 포맷팅 (예: 1000 -> 1,000)
 */
export function formatNumber(num: number): string {
  return new Intl.NumberFormat("ko-KR").format(num);
}

/**
 * 평점을 별 문자열로 변환 (예: 4.5 -> "★★★★☆")
 */
export function formatStars(rating: number, maxStars: number = 5): string {
  const fullStars = Math.floor(rating);
  const hasHalfStar = rating % 1 >= 0.5;
  const emptyStars = maxStars - fullStars - (hasHalfStar ? 1 : 0);

  return (
    "★".repeat(fullStars) + (hasHalfStar ? "☆" : "") + "☆".repeat(emptyStars)
  );
}

/**
 * 상대적 시간 표시 (예: "2시간 전", "3일 전")
 */
export function formatRelativeTime(date: Date | string): string {
  const now = new Date();
  const targetDate = typeof date === "string" ? new Date(date) : date;
  const diffInMs = now.getTime() - targetDate.getTime();

  const minute = 60 * 1000;
  const hour = minute * 60;
  const day = hour * 24;
  const week = day * 7;
  const month = day * 30;
  const year = day * 365;

  if (diffInMs < minute) {
    return "방금 전";
  } else if (diffInMs < hour) {
    return `${Math.floor(diffInMs / minute)}분 전`;
  } else if (diffInMs < day) {
    return `${Math.floor(diffInMs / hour)}시간 전`;
  } else if (diffInMs < week) {
    return `${Math.floor(diffInMs / day)}일 전`;
  } else if (diffInMs < month) {
    return `${Math.floor(diffInMs / week)}주 전`;
  } else if (diffInMs < year) {
    return `${Math.floor(diffInMs / month)}개월 전`;
  } else {
    return `${Math.floor(diffInMs / year)}년 전`;
  }
}

/**
 * 날짜를 한국어 형식으로 포맷팅 (예: "2024년 3월 15일")
 */
export function formatDate(date: Date | string): string {
  const targetDate = typeof date === "string" ? new Date(date) : date;
  return new Intl.DateTimeFormat("ko-KR", {
    year: "numeric",
    month: "long",
    day: "numeric",
  }).format(targetDate);
}

/**
 * 영화 런타임을 시간:분 형식으로 변환 (예: 150 -> "2시간 30분")
 */
export function formatRuntime(minutes: number): string {
  if (minutes < 60) {
    return `${minutes}분`;
  }

  const hours = Math.floor(minutes / 60);
  const remainingMinutes = minutes % 60;

  if (remainingMinutes === 0) {
    return `${hours}시간`;
  }

  return `${hours}시간 ${remainingMinutes}분`;
}

/**
 * TMDB 이미지 URL 생성
 */
export function getTMDBImageUrl(
  path: string | null,
  size: string = "w500"
): string {
  if (!path) return "/images/placeholder-movie.jpg";
  return `${
    process.env.NEXT_PUBLIC_TMDB_IMAGE_URL?.replace("w500", size) ||
    "https://image.tmdb.org/t/p/w500"
  }${path}`;
}

/**
 * 문자열 자르기 (말줄임표 추가)
 */
export function truncateText(text: string, maxLength: number): string {
  if (text.length <= maxLength) return text;
  return text.substring(0, maxLength) + "...";
}

/**
 * 객체를 FormData로 변환
 */
export function objectToFormData(obj: Record<string, unknown>): FormData {
  const formData = new FormData();

  Object.entries(obj).forEach(([key, value]) => {
    if (value !== null && value !== undefined) {
      if (value instanceof File) {
        formData.append(key, value);
      } else if (Array.isArray(value)) {
        value.forEach((item, index) => {
          formData.append(`${key}[${index}]`, String(item));
        });
      } else {
        formData.append(key, String(value));
      }
    }
  });

  return formData;
}

/**
 * 디바운스 함수
 */
export function debounce<T extends (...args: unknown[]) => void>(
  func: T,
  wait: number
): (...args: Parameters<T>) => void {
  let timeout: NodeJS.Timeout;

  return (...args: Parameters<T>) => {
    clearTimeout(timeout);
    timeout = setTimeout(() => func(...args), wait);
  };
}

/**
 * 배열을 청크 단위로 나누기
 */
export function chunkArray<T>(array: T[], chunkSize: number): T[][] {
  const chunks: T[][] = [];
  for (let i = 0; i < array.length; i += chunkSize) {
    chunks.push(array.slice(i, i + chunkSize));
  }
  return chunks;
}

/**
 * 랜덤 배열 요소 선택
 */
export function getRandomElements<T>(array: T[], count: number): T[] {
  const shuffled = [...array].sort(() => 0.5 - Math.random());
  return shuffled.slice(0, count);
}

/**
 * URL 쿼리 파라미터를 객체로 변환
 */
export function parseQueryParams(queryString: string): Record<string, string> {
  const params = new URLSearchParams(queryString);
  const result: Record<string, string> = {};

  // URLSearchParams.entries()를 배열로 변환하여 반복
  Array.from(params.entries()).forEach(([key, value]) => {
    result[key] = value;
  });

  return result;
}

/**
 * 객체를 URL 쿼리 스트링으로 변환
 */
export function objectToQueryString(obj: Record<string, unknown>): string {
  const params = new URLSearchParams();

  Object.entries(obj).forEach(([key, value]) => {
    if (value !== null && value !== undefined && value !== "") {
      params.append(key, String(value));
    }
  });

  return params.toString();
}
