package com.caro.bizkit.domain.auth.controller;

import com.caro.bizkit.domain.auth.dto.LoginRequest;
import com.caro.bizkit.domain.auth.service.AuthService;
import com.caro.bizkit.security.JwtProperties;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@Slf4j
@Tag(name = "Auth", description = "인증/인가 API")
public class AuthController {

    private final AuthService authService;
    private final JwtProperties jwtProperties;
    private final boolean cookieSecure;

    public AuthController(
            AuthService authService,
            JwtProperties jwtProperties,
            @Value("${cookie.secure:true}") boolean cookieSecure
    ) {
        this.authService = authService;
        this.jwtProperties = jwtProperties;
        this.cookieSecure = cookieSecure;
    }

    @PostMapping("/login/{provider}")
    @Operation(summary = "로그인", description = "pathvariable로 소셜 서비스 회사명(kakao)를 받고 body로 " +
            "소셜 로그인 코드를 받아 액세스 토큰을 HttpOnly 쿠키로 발급합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "로그인 성공"
            )
    })
    public ResponseEntity<Void> login(
            @Parameter(description = "소셜 로그인 제공자", example = "kakao")
            @PathVariable String provider,
            @Valid @RequestBody LoginRequest request,
            HttpServletResponse response
    ) {
        String token = authService.login(provider, request.code(), request.redirectUri());

        ResponseCookie cookie = ResponseCookie.from("accessToken", token)
                .httpOnly(true)
                .secure(cookieSecure)
                .sameSite("Lax")
                .path("/")
                .maxAge(jwtProperties.getAccessTokenValiditySeconds())
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/kakao/callback")
    @Operation(summary = "카카오 콜백", description = "카카오 OAuth 콜백 테스트용 엔드포인트입니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "콜백 수신"
            )
    })
    public ResponseEntity<String> kakaoCallback(@RequestParam String code) {
        return ResponseEntity.ok(code);
    }


}
