package com.caro.bizkit.domain.user.controller;

import com.caro.bizkit.common.ApiResponse.ApiResponse;
import com.caro.bizkit.domain.user.dto.UserPrincipal;
import com.caro.bizkit.domain.user.dto.UserRequest;
import com.caro.bizkit.domain.user.dto.UserResponse;
import com.caro.bizkit.domain.user.service.UserService;
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
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = UserResponse.class))
            )
    })
    public ResponseEntity<ApiResponse<UserResponse>> getMyStatus(@AuthenticationPrincipal UserPrincipal user) {
        UserResponse userResponse =  userService.getMyStatus(user);
        return ResponseEntity.ok(ApiResponse.success("내 정보 조회 성공", userResponse));
    }

    @PutMapping("/me")
    public ResponseEntity<?> updateMyStatus(
            @AuthenticationPrincipal UserPrincipal user,
            @RequestBody UserRequest request
    ) {
        UserResponse userResponse = userService.updateMyStatus(user, request);
        return ResponseEntity.ok(ApiResponse.success("내 정보 수정 성공", userResponse));
    }

}
