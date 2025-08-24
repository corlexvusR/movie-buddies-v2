/**
 * API 응답 관련 TypeScript 타입 정의
 * 백엔드 ApiResponse<T> 및 Spring Data Page 구조와 일치
 */

/**
 * 백엔드 ApiResponse와 일치하는 표준 API 응답 래퍼 타입
 * 백엔드의 ApiResponse<T> 클래스와 동일한 구조
 */
export interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
  errorCode?: string;
  timestamp: string;
}

/**
 * Spring Data Page 구조와 정확히 일치하는 페이징 응답 타입
 */
export interface PageResponse<T> {
  content: T[];
  pageable: {
    offset: number;
    pageSize: number;
    pageNumber: number;
    paged: boolean;
    unpaged: boolean;
    sort: {
      empty: boolean;
      sorted: boolean;
      unsorted: boolean;
    };
  };
  totalElements: number;
  totalPages: number;
  last: boolean;
  first: boolean;
  size: number;
  number: number;
  numberOfElements: number;
  empty: boolean;
  sort: {
    empty: boolean;
    sorted: boolean;
    unsorted: boolean;
  };
}

/**
 * API 에러 응답 타입
 * 폼 검증 에러도 지원
 */
export interface ApiError {
  success: false;
  message: string;
  errorCode?: string;
  timestamp: string;
  errors?: Record<string, string[]>; // 필드별 검증 에러 (예: username: ["이미 사용중인 사용자명입니다"])
}

/**
 * HTTP 메서드 타입
 */
export type HttpMethod = "GET" | "POST" | "PUT" | "DELETE" | "PATCH";

/**
 * 정렬 방향 타입
 */
export type SortDirection = "asc" | "desc";

/**
 * 페이징 요청 파라미터
 */
export interface PaginationParams {
  page?: number;
  size?: number;
  sort?: string;
  direction?: SortDirection;
}

/**
 * API 엔드포인트 상수
 * lib/api.ts에서 사용하기 위한 경로 정의
 */
export const API_ENDPOINTS = {
  // 인증
  AUTH: {
    SIGNUP: "/auth/signup",
    LOGIN: "/auth/login",
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
    PROFILE_IMAGE: "/users/me/profile-image",
    CHANGE_PASSWORD: "/users/me/password",
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
    REMOVE: (username: string) => `/friends/${username}`,
    SENT_REQUESTS: "/friends/sent-requests",
    RECEIVED_REQUESTS: "/friends/received-requests",
    RESPOND: (username: string) => `/friends/request/${username}/response`,
  },

  // 채팅 (REST API + WebSocket)
  CHAT: {
    // REST API 엔드포인트
    ROOMS: "/chat/rooms", // 전체 채팅방 목록 (페이징)
    MY_ROOMS: "/chat/rooms/my", // 내 참여 채팅방 목록
    CREATE_ROOM: "/chat/rooms", // 채팅방 생성 (POST)
    ROOM_DETAIL: (roomId: number) => `/chat/rooms/${roomId}`, // 채팅방 상세 정보
    MESSAGES: (roomId: number) => `/chat/rooms/${roomId}/messages`, // 메시지 히스토리 (페이징)
    JOIN_ROOM: (roomId: number) => `/chat/rooms/${roomId}/join`, // 채팅방 입장 (POST)
    LEAVE_ROOM: (roomId: number) => `/chat/rooms/${roomId}/leave`, // 채팅방 퇴장 (POST)

    // WebSocket 엔드포인트 (STOMP 프로토콜)
    WEBSOCKET: {
      CONNECT: "/ws", // WebSocket 연결 엔드포인트
      // 메시지 전송 (클라이언트 -> 서버)
      SEND_MESSAGE: (roomId: number) => `/app/chat/${roomId}/send`, // 메시지 전송
      JOIN_ROOM_WS: (roomId: number) => `/app/chat/${roomId}/join`, // WebSocket 입장
      LEAVE_ROOM_WS: (roomId: number) => `/app/chat/${roomId}/leave`, // WebSocket 퇴장
      // 구독 (서버 -> 클라이언트)
      SUBSCRIBE_ROOM: (roomId: number) => `/topic/chat/${roomId}`, // 채팅방 메시지 구독
    },
  },
} as const;
