package com.moviebuddies.controller;

import com.moviebuddies.dto.request.FriendRequestDto;
import com.moviebuddies.dto.response.ApiResponse;
import com.moviebuddies.dto.response.FriendResponse;
import com.moviebuddies.security.UserDetailsImpl;
import com.moviebuddies.service.FriendService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/friends")
@RequiredArgsConstructor
@Tag(name = "Friend", description = "친구 관리 API")
public class FriendController {

    private final FriendService friendService;

    @Operation(summary = "친구 요청", description = "다른 사용자에게 친구 요청을 보냅니다.")
    @SecurityRequirement(name = "Bearer Authentication")
    @PostMapping("/request")
    public ResponseEntity<ApiResponse<FriendResponse>> sendFriendRequest(@RequestBody FriendRequestDto request, @AuthenticationPrincipal UserDetailsImpl userDetails) {

        FriendResponse friendResponse = friendService.sendFriendRequest(userDetails.getId(), request.getUsername());
        return ResponseEntity.ok(ApiResponse.success("친구 요청을 보냈습니다.", friendResponse));
    }

    @Operation(summary = "친구 요청 응답", description = "받은 친구 요청에 응답합니다.")
    @SecurityRequirement(name = "Bearer Authentication")
    @PutMapping("/request/{username}/response")
    public ResponseEntity<ApiResponse<Void>> respondToFriendRequest(@PathVariable String username, @RequestParam boolean accept, @AuthenticationPrincipal UserDetailsImpl userDetails) {

        if (accept) {
            friendService.acceptFriendRequest(userDetails.getId(), username);
            return ResponseEntity.ok(ApiResponse.success("친구 요청을 수락했습니다."));
        } else {
            friendService.declineFriendRequest(userDetails.getId(), username);
            return ResponseEntity.ok(ApiResponse.success("친구 요청을 거절했습니다."));
        }
    }

    @Operation(summary = "친구 목록", description = "내 친구 목록을 조회합니다.")
    @SecurityRequirement(name = "Bearer Authentication")
    @GetMapping("")
    public ResponseEntity<ApiResponse<List<FriendResponse>>> getFriends(@AuthenticationPrincipal UserDetailsImpl userDetails) {

        List<FriendResponse> friends = friendService.getFriends(userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success("친구 목록입니다.", friends));
    }

    @Operation(summary = "보낸 친구 요청 목록", description = "내가 보낸 친구 요청 목록을 조회합니다.")
    @SecurityRequirement(name = "Bearer Authentication")
    @GetMapping("/sent-requests")
    public ResponseEntity<ApiResponse<List<FriendResponse>>> getSentRequests(@AuthenticationPrincipal UserDetailsImpl userDetails) {

        List<FriendResponse> sentRequests = friendService.getSentRequests(userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success("보낸 친구 요청 목록입니다.", sentRequests));
    }

    @Operation(summary = "받은 친구 요청 목록", description = "내가 받은 친구 요청 목록을 조회합니다.")
    @SecurityRequirement(name = "Bearer Authentication")
    @GetMapping("/received-requests")
    public ResponseEntity<ApiResponse<List<FriendResponse>>> getReceivedRequests(@AuthenticationPrincipal UserDetailsImpl userDetails) {

        List<FriendResponse> receivedRequests = friendService.getReceivedRequests(userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success("받은 친구 요청 목록입니다.", receivedRequests));
    }

    @Operation(summary = "친구 삭제", description = "친구를 삭제합니다.")
    @SecurityRequirement(name = "Bearer Authentication")
    @DeleteMapping("/{username}")
    public ResponseEntity<ApiResponse<Void>> removeFriend(@PathVariable String username, @AuthenticationPrincipal UserDetailsImpl userDetails) {

        friendService.removeFriend(userDetails.getId(), username);
        return ResponseEntity.ok(ApiResponse.success("친구를 삭제했습니다."));
    }

}
