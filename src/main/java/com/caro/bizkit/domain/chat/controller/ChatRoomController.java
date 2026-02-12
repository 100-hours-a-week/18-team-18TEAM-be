package com.caro.bizkit.domain.chat.controller;

import com.caro.bizkit.common.ApiResponse.ApiResponse;
import com.caro.bizkit.domain.chat.dto.ChatRoomCreateRequest;
import com.caro.bizkit.domain.chat.dto.ChatRoomResponse;
import com.caro.bizkit.domain.chat.service.ChatRoomService;
import com.caro.bizkit.domain.user.dto.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/chat/rooms")
@RequiredArgsConstructor
@Tag(name = "Chat Room", description = "채팅방 API")
public class ChatRoomController {

    private final ChatRoomService chatRoomService;

    @PostMapping
    @Operation(summary = "채팅방 생성", description = "1:1 채팅방을 생성합니다. 기존 방이 있으면 반환합니다.")
    public ResponseEntity<ApiResponse<ChatRoomResponse>> createRoom(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody ChatRoomCreateRequest request
    ) {
        ChatRoomResponse response = chatRoomService.createRoom(principal, request);
        return ResponseEntity.ok(ApiResponse.success("채팅방 생성 성공", response));
    }

    @GetMapping
    @Operation(summary = "내 채팅방 목록", description = "참여 중인 채팅방 목록을 최신 메시지순으로 조회합니다.")
    public ResponseEntity<ApiResponse<List<ChatRoomResponse>>> getMyRooms(
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        List<ChatRoomResponse> responses = chatRoomService.getMyRooms(principal);
        return ResponseEntity.ok(ApiResponse.success("채팅방 목록 조회 성공", responses));
    }

    @DeleteMapping("/{room_id}")
    @Operation(summary = "채팅방 나가기", description = "채팅방에서 논리적으로 퇴장합니다.")
    public ResponseEntity<ApiResponse<Void>> leaveRoom(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable("room_id") Integer roomId
    ) {
        chatRoomService.leaveRoom(principal, roomId);
        return ResponseEntity.ok(ApiResponse.success("채팅방 나가기 성공", null));
    }
}
