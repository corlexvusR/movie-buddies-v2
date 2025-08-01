package com.moviebuddies.dto.response;

import com.moviebuddies.entity.Friend;
import com.moviebuddies.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 친구 관계 응답 DTO
 *
 * 친구 관계 정보를 클라이언트에 전달하기 위한 데이터 전송 객체
 * 양방향 친구 관계에서 현재 사용자를 기준으로 상대방 정보를 제공
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FriendResponse {

    /**
     * 친구 관계 ID
     */
    private Long id;

    /**
     * 상대방 사용자 정보 (현재 사용자가 아닌 친구)
     */
    private UserResponse friend;

    /**
     * 친구 관계 상태 (PENDING, ACCEPTED, DECLINED)
     */
    private String status;

    /**
     * 친구 요청 생성 시간
     */
    private LocalDateTime createdAt;

    /**
     * 친구 요청 수락 시간
     */
    private LocalDateTime acceptedAt;

    /**
     * 현재 사용자가 요청자인지 여부
     */
    private boolean isRequester;

    /**
     * Friend 엔티티를 FriendResponse DTO로 변환
     *
     * 양방향 친구 관계에서 현재 사용자를 기준으로 상대방을 friend로 설정
     * 요청자가 수신자 중 현재 사용지가 아닌 상대방을 친구로 표시
     * 
     * @param friendEntity 친구 관계 엔티티
     * @param currentUser 현재 로그인한 사용자
     * @return 변환된 FriendReponse 객체
     */
    public static FriendResponse from(Friend friendEntity, User currentUser) {

        // 현재 사용자가 아닌 상대방을 friend로 설정
        User friendUser = friendEntity.getRequester().equals(currentUser)
                ? friendEntity.getReceiver()
                : friendEntity.getRequester();

        boolean isRequester = friendEntity.getRequester().equals(currentUser);

        return FriendResponse.builder()
                .id(friendEntity.getId())
                .friend(UserResponse.from(friendUser))
                .status(friendEntity.getStatus().name())
                .createdAt(friendEntity.getCreateAt())
                .acceptedAt(friendEntity.getAcceptedAt())
                .isRequester(isRequester)
                .build();
    }
}
