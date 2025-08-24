import { MovieListResponse } from "./movie";

/**
 * 북마크 응답 타입 (백엔드 BookmarkResponse와 일치)
 */
export interface BookmarkResponse {
  id: number;
  movie: MovieListResponse; // 전체 MovieListResponse 구조
  createdAt: string;
}

/**
 * 영화 북마크 통계 응답 타입 (백엔드 MovieBookmarkStatsResponse와 일치)
 */
export interface MovieBookmarkStatsResponse {
  movieId: number;
  movieTitle: string;
  bookmarkCount: number;
}
