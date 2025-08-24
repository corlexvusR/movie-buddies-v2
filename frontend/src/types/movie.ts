/**
 * 장르 응답 타입 (백엔드 GenreResponse와 일치)
 */
export interface GenreResponse {
  id: number;
  name: string;
  tmdbId: number;
}

/**
 * 배우 응답 타입 (백엔드 ActorResponse와 일치)
 */
export interface ActorResponse {
  id: number;
  name: string;
  profileImageUrl?: string;
  popularity: number;
  tmdbId: number;
}

/**
 * 영화 목록 응답 타입 (백엔드 MovieListResponse와 일치)
 */
export interface MovieListResponse {
  id: number;
  title: string;
  releaseDate?: string;
  popularity: number;
  voteCount: number;
  voteAverage: number;
  overview?: string;
  posterImageUrl?: string;
  runtime: number;
  isNowPlaying: boolean;
  genres: GenreResponse[];
  reviewCount: number;
  bookmarkCount: number;
}

/**
 * 영화 상세 응답 타입 (백엔드 MovieResponse와 일치)
 */
export interface MovieResponse {
  id: number;
  title: string;
  releaseDate?: string;
  popularity: number;
  voteCount: number;
  voteAverage: number;
  overview?: string;
  posterImageUrl?: string;
  backdropImageUrl?: string;
  runtime: number;
  isNowPlaying: boolean;
  tmdbId: number;
  createdAt: string;
  genres: GenreResponse[];
  actors: ActorResponse[];
  averageRating?: number;
  reviewCount: number;
  bookmarkCount: number;
}

/**
 * 영화 검색 요청 타입 (백엔드 MovieSearchRequest와 정확히 일치)
 */
export interface MovieSearchRequest {
  title?: string;
  genreId?: number;
  actorName?: string;
  releaseYear?: number; // year가 아닌 releaseYear
  minRating?: number;
  maxRating?: number;
  minRuntime?: number;
  maxRuntime?: number;
  nowPlaying?: boolean; // 추가된 필드
}

/**
 * 영화 평점 분포 응답 타입 (백엔드의 MovieRatingDistributionResponse와 일치)
 */
export interface MovieRatingDistributionResponse {
  movieId: number;
  ratingDistribution: Array<[number, number]>; // [평점, 개수] 튜플 배열
}

/**
 * 영화 북마크 통계 응답 타입 (백엔드의 MovieBookmarkStatsResponse와 일치)
 */
export interface MovieBookmarkStatsResponse {
  movieId: number;
  movieTitle: string;
  bookmarkCount: number;
}
