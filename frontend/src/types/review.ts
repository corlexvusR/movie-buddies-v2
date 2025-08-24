/**
 * 리뷰 요청 타입 (백엔드 ReviewRequest와 일치)
 */
export interface ReviewRequest {
  movieId: number;
  content: string;
  rating: number; // 1-5
}

/**
 * 리뷰 응답 타입 (백엔드 ReviewResponse와 일치)
 */
export interface ReviewResponse {
  id: number;
  movieId: number;
  movieTitle: string;
  moviePosterUrl?: string;
  userId: number;
  username: string;
  userNickname: string;
  userProfileImageUrl?: string;
  content: string;
  rating: number;
  createdAt: string;
  lastModifiedAt: string;
}
