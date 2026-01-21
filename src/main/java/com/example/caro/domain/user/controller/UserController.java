package com.example.caro.domain.user.controller;

import com.example.caro.common.ApiResponse.ApiResponse;
import com.example.caro.domain.user.dto.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    @GetMapping("/me")
    public ResponseEntity<?> getMyStatus(@AuthenticationPrincipal UserResponse user) {

        return ResponseEntity.ok(ApiResponse.success("내 정보 조회 성공", user));
    }

}
