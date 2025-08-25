// API 엔드포인트
export const API_ENDPOINTS = {
  // 인증
  AUTH: {
    LOGIN: "/auth/login",
    SIGNUP: "/auth/signup",
    LOGOUT: "/auth/logout",
    REFRESH: "/auth/refresh",
    CHECK_USERNAME: "/auth/check/username",
    CHECK_EMAIL: "/auth/check/email",
    CHECK_NICKNAME: "/auth/check/nickname",
  },

  // 사용자
  USERS: {
    ME: "/users/me",
    PROFILE: (username: string) => `/users/${username}`,
    SEARCH: "/users/search",
    UPLOAD_PROFILE_IMAGE: "/users/me/profile-image",
  },

  // 영화
  MOVIES: {
    LIST: "/movies",
    DETAIL: (id: number) => `/movies/${id}`,
    SEARCH: "/movies/search",
    BY_GENRE: (genreId: number) => `/movies/genre/${genreId}`,
    BY_ACTOR: (actorId: number) => `/movies/actor/${actorId}`,
    BY_YEAR: (year: number) => `/movies/year/${year}`,
    BY_RATING: "/movies/rating",
    BY_RUNTIME: "/movies/runtime",
    RECOMMENDATIONS: (id: number) => `/movies/${id}/recommendations`,
    NOW_PLAYING_TOP5: "/movies/ranking/now-playing",
    GENRE_TOP5: (genreId: number) => `/movies/ranking/genre/${genreId}`,
  },

  // 북마크
  BOOKMARKS: {
    MY: "/bookmarks/my",
    USER: (username: string) => `/bookmarks/user/${username}`,
    ADD: (movieId: number) => `/bookmarks/movies/${movieId}`,
    REMOVE: (movieId: number) => `/bookmarks/movies/${movieId}`,
    CHECK: (movieId: number) => `/bookmarks/movies/${movieId}/check`,
    BY_GENRE: (genreId: number) => `/bookmarks/genre/${genreId}`,
    POPULAR: "/bookmarks/popular",
  },

  // 리뷰
  REVIEWS: {
    CREATE: "/reviews",
    BY_MOVIE: (movieId: number) => `/reviews/movie/${movieId}`,
    BY_USER: (username: string) => `/reviews/user/${username}`,
    MY_FOR_MOVIE: (movieId: number) => `/reviews/my/movie/${movieId}`,
    UPDATE: (movieId: number) => `/reviews/movie/${movieId}`,
    DELETE: (movieId: number, reviewId: number) =>
      `/reviews/movie/${movieId}/${reviewId}`,
    RATING_DISTRIBUTION: (movieId: number) =>
      `/reviews/movie/${movieId}/rating-distribution`,
  },

  // 친구
  FRIENDS: {
    LIST: "/friends",
    REQUEST: "/friends/request",
    SENT_REQUESTS: "/friends/sent-requests",
    RECEIVED_REQUESTS: "/friends/received-requests",
    RESPOND: (username: string) => `/friends/request/${username}/response`,
    REMOVE: (username: string) => `/friends/${username}`,
  },

  // 채팅 (웹소켓)
  CHAT: {
    ROOMS: "/chat/rooms",
    ROOM_DETAIL: (roomId: number) => `/chat/rooms/${roomId}`,
    CREATE_ROOM: "/chat/rooms",
    MESSAGES: (roomId: number) => `/chat/rooms/${roomId}/messages`,
  },
} as const;

// 페이지 라우트
export const ROUTES = {
  HOME: "/",

  // 인증
  LOGIN: "/auth/login",
  SIGNUP: "/auth/signup",

  // 영화
  MOVIES: "/movies",
  MOVIE_DETAIL: (id: number) => `/movies/${id}`,
  MOVIES_SEARCH: "/movies/search",
  MOVIES_GENRES: "/movies/genres",
  MOVIES_GENRE: (genreId: number) => `/movies/genres/${genreId}`,

  // 사용자
  PROFILE: (username: string) => `/profile/${username}`,
  PROFILE_EDIT: "/profile/edit",
  PROFILE_SETTINGS: "/profile/settings",

  // 추천
  RECOMMENDATIONS: "/recommendations",

  // 채팅
  CHAT: "/chat",
  CHAT_ROOM: (roomId: number) => `/chat/${roomId}`,

  // 기타
  HELP: "/help",
  CONTACT: "/contact",
  PRIVACY: "/privacy",
  TERMS: "/terms",
} as const;

// 로컬 스토리지 키
export const STORAGE_KEYS = {
  TOKEN: "token",
  REFRESH_TOKEN: "refreshToken",
  USER: "user",
  THEME: "theme",
  LANGUAGE: "language",
  SEARCH_HISTORY: "searchHistory",
} as const;

// 페이지 크기 옵션
export const PAGE_SIZES = [10, 20, 50, 100] as const;

// 기본 페이지 크기
export const DEFAULT_PAGE_SIZE = 20;

// 평점 범위
export const RATING_RANGE = {
  MIN: 1,
  MAX: 5,
} as const;

// 파일 업로드 제한
export const FILE_LIMITS = {
  PROFILE_IMAGE: {
    MAX_SIZE: 5 * 1024 * 1024, // 5MB
    ALLOWED_TYPES: ["image/jpeg", "image/jpg", "image/png", "image/webp"],
  },
  CHAT_IMAGE: {
    MAX_SIZE: 10 * 1024 * 1024, // 10MB
    ALLOWED_TYPES: [
      "image/jpeg",
      "image/jpg",
      "image/png",
      "image/webp",
      "image/gif",
    ],
  },
} as const;

// 디바운스 지연 시간
export const DEBOUNCE_DELAYS = {
  SEARCH: 300,
  TYPING: 500,
  SCROLL: 100,
} as const;

// 토스트 기본 지속 시간
export const TOAST_DURATION = {
  SUCCESS: 3000,
  ERROR: 5000,
  WARNING: 4000,
  INFO: 4000,
} as const;

// 장르 ID 매핑 (TMDB 기준)
export const GENRE_IDS = {
  ACTION: 28,
  ADVENTURE: 12,
  ANIMATION: 16,
  COMEDY: 35,
  CRIME: 80,
  DOCUMENTARY: 99,
  DRAMA: 18,
  FAMILY: 10751,
  FANTASY: 14,
  HISTORY: 36,
  HORROR: 27,
  MUSIC: 10402,
  MYSTERY: 9648,
  ROMANCE: 10749,
  SCIENCE_FICTION: 878,
  TV_MOVIE: 10770,
  THRILLER: 53,
  WAR: 10752,
  WESTERN: 37,
} as const;

// 환경 변수
export const ENV = {
  API_URL: process.env.NEXT_PUBLIC_API_URL || "http://localhost:8080/api/v1",
  TMDB_IMAGE_URL:
    process.env.NEXT_PUBLIC_TMDB_IMAGE_URL || "https://image.tmdb.org/t/p/w500",
  WS_URL: process.env.NEXT_PUBLIC_WS_URL || "ws://localhost:8080/ws",
  NODE_ENV: process.env.NODE_ENV || "development",
} as const;

// 메타데이터
export const METADATA = {
  SITE_NAME: "MovieBuddies",
  DESCRIPTION: "영화 정보 플랫폼",
  KEYWORDS: ["영화", "리뷰", "추천", "소셜", "영화정보"],
  AUTHOR: "MovieBuddies Team",
} as const;
