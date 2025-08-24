/**
 * 채팅 메시지 응답 타입 (백엔드 ChatMessageResponse와 일치)
 */
export interface ChatMessageResponse {
  id: number; // Long -> number
  chatRoomId: number;
  senderDisplayName: string; // 익명화된 표시명
  content: string;
  createdAt: string; // LocalDateTime -> string
  isSystem?: boolean; // 시스템 메시지 여부
}

/**
 * 채팅방 응답 타입 (백엔드 ChatRoomResponse와 일치)
 */
export interface ChatRoomResponse {
  id: number;
  name: string;
  description?: string;
  maxParticipants: number;
  currentParticipants: number;
  isActive: boolean;
  createdByDisplayName: string; // 생성자 익명화된 표시명
  createdAt: string;
}

/**
 * 채팅방 생성 요청 타입 (백엔드 ChatRoomCreateRequest와 일치)
 */
export interface ChatRoomCreateRequest {
  name: string;
  description?: string;
  maxParticipants?: number; // 기본값 50
}

/**
 * 채팅 메시지 전송 요청 타입 (백엔드 ChatMessageRequest와 일치)
 */
export interface ChatMessageRequest {
  content: string; // 텍스트 메시지만 지원
}

/**
 * WebSocket 메시지 타입 (STOMP 프로토콜용)
 */
export interface WebSocketMessage {
  type: "MESSAGE" | "JOIN" | "LEAVE";
  roomId: number;
  data: ChatMessageRequest;
}
