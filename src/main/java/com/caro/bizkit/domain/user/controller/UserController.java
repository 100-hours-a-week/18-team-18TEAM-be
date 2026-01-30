package com.caro.bizkit.domain.user.controller;

import com.caro.bizkit.common.ApiResponse.ApiResponse;
import com.caro.bizkit.domain.user.dto.UserPrincipal;
import com.caro.bizkit.domain.user.dto.UserResponse;

import com.caro.bizkit.domain.user.service.UserService;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "User", description = "사용자 정보 조회 API")
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    @Operation(summary = "내 정보 조회", description = "인증된 사용자의 기본 정보를 조회합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200")
    })
    public ResponseEntity<ApiResponse<UserResponse>> getMyStatus(@AuthenticationPrincipal UserPrincipal user) {
        UserResponse userResponse =  userService.getMyStatus(user);
        return ResponseEntity.ok(ApiResponse.success("내 정보 조회 성공", userResponse));
    }

    @GetMapping("/{user_id}")
    @Operation(summary = "상대 정보 조회", description = "상대 사용자의 기본 정보를 조회합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200")
    })
    public ResponseEntity<ApiResponse<UserResponse>> getUserProfile(@PathVariable("user_id") Integer userId) {
        UserResponse userResponse = userService.getUserProfile(userId);
        if (userResponse == null) {
            return ResponseEntity.ok(ApiResponse.success("탈퇴한 회원입니다", null));
        }
        return ResponseEntity.ok(ApiResponse.success("사용자 정보 조회 성공", userResponse));
    }

    @PatchMapping("/me")
    @Operation(summary = "내 정보 수정", description = "내 사용자 정보를 수정합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200")
    })
    public ResponseEntity<?> updateMyStatus(
            @AuthenticationPrincipal UserPrincipal user,
            @RequestBody Map<String, Object> request
    ) {
        UserResponse userResponse = userService.updateMyStatus(user, request);
        return ResponseEntity.ok(ApiResponse.success("내 정보 수정 성공", userResponse));
    }
    @DeleteMapping("/me")
    @Operation(summary = "회원 탈퇴", description = "카카오 연결 해제 후 계정을 탈퇴 처리합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200")
    })
    public ResponseEntity<ApiResponse<Void>> withdraw(
            @AuthenticationPrincipal UserPrincipal user
    ) {
        userService.withdraw(user);
        return ResponseEntity.ok(ApiResponse.success("회원 탈퇴 성공", null));
    }

}
