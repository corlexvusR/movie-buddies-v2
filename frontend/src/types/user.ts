import { UserResponse } from "./auth";

/**
 * 사용자 업데이트 요청 타입 (백엔드 UserUpdateRequest와 일치)
 */
export interface UserUpdateRequest {
  nickname?: string;
  email?: string;
  // profileImage는 별도 API로 처리 (multipart/form-data)
}

/**
 * 친구 관계 상태 (백엔드 enum과 일치)
 */
export type FriendStatus = "PENDING" | "ACCEPTED" | "DECLINED";

/**
 * 친구 요청 타입 (백엔드 FriendRequestDto와 일치)
 */
export interface FriendRequestDto {
  username: string;
}

/**
 * 친구 응답 타입 (백엔드 FriendResponse와 일치)
 */
export interface FriendResponse {
  id: number;
  friend: UserResponse; // 상대방 정보
  status: string; // FriendStatus enum
  createdAt: string; // createAt 오타 수정
  acceptedAt?: string;
  isRequester: boolean; // 현재 사용자가 요청자인지
}

// UserResponse는 auth.ts에서 import (중복 방지)
