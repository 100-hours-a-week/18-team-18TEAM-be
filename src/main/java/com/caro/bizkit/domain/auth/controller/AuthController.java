package com.caro.bizkit.domain.auth.controller;

import com.caro.bizkit.common.ApiResponse.ApiResponse;
import com.caro.bizkit.domain.auth.dto.LoginRequest;
import com.caro.bizkit.domain.auth.dto.RefreshRequest;
import com.caro.bizkit.domain.auth.dto.TokenPair;
import com.caro.bizkit.domain.auth.service.AuthService;
import com.caro.bizkit.domain.user.dto.UserPrincipal;
import com.caro.bizkit.security.JwtProperties;
import com.caro.bizkit.security.JwtTokenProvider;
import com.caro.bizkit.security.RefreshTokenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.util.Arrays;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/auth")
@Slf4j
@Tag(name = "Auth", description = "인증/인가 API")
public class AuthController {

    private final AuthService authService;
    private final JwtProperties jwtProperties;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenService refreshTokenService;
    private final boolean cookieSecure;
    private final String cookieSameSite;

    public AuthController(
            AuthService authService,
            JwtProperties jwtProperties,
            JwtTokenProvider jwtTokenProvider,
            RefreshTokenService refreshTokenService,
            @Value("${cookie.secure:true}") boolean cookieSecure,
            @Value("${cookie.same-site:Lax}") String cookieSameSite
    ) {
        this.authService = authService;
        this.jwtProperties = jwtProperties;
        this.jwtTokenProvider = jwtTokenProvider;
        this.refreshTokenService = refreshTokenService;
        this.cookieSecure = cookieSecure;
        this.cookieSameSite = cookieSameSite;
    }

    @PostMapping("/login/{provider}")
    @Operation(summary = "로그인", description = "pathvariable로 소셜 서비스 회사명(kakao)를 받고 body로 " +
            "소셜 로그인 코드와 redirect_uri를 받아 액세스 토큰과 리프레시 토큰을 HttpOnly 쿠키 그리고 바디로 발급합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "로그인 성공"
            )
    })
    public ResponseEntity<?> login(
            @Parameter(description = "소셜 로그인 제공자", example = "kakao")
            @PathVariable String provider,
            @Valid @RequestBody LoginRequest request,
            HttpServletResponse response
    ) {
        TokenPair tokenPair = authService.login(provider, request.code(), request.redirectUri());

        // 기존 쿠키 삭제 (다양한 path로 설정된 쿠키들 정리)
        clearAuthCookies(response);

        // 새 쿠키 발급
        ResponseCookie accessCookie = buildCookie("accessToken", tokenPair.accessToken(), jwtProperties.getAccessTokenValiditySeconds());
        ResponseCookie refreshCookie = buildCookie("refreshToken", tokenPair.refreshToken(), refreshTokenService.getRefreshTokenValiditySeconds());

        response.addHeader(HttpHeaders.SET_COOKIE, accessCookie.toString());
        response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());
        return ResponseEntity.ok().body(ApiResponse.success("로그인 성공", tokenPair));
    }

    @PostMapping("/rotation")
    @Operation(summary = "토큰 갱신", description = "RefreshToken만으로 새로운 AccessToken과 RefreshToken을 발급합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "토큰 갱신 성공"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "유효하지 않은 RefreshToken"
            )
    })
    public ResponseEntity<?> refresh(
            @RequestBody(required = false) RefreshRequest refreshRequest,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        String refreshToken = (refreshRequest != null && refreshRequest.refreshToken() != null)
                ? refreshRequest.refreshToken()
                : extractCookie(request, "refreshToken");

        if (refreshToken == null) {
            log.warn("토큰 재발행 실패: 리프레시 토큰 없음");
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "리프레시 토큰이 없습니다.");
        }

        try {
            TokenPair tokenPair = authService.refresh(refreshToken);

            clearAuthCookies(response);

            ResponseCookie accessCookie = buildCookie("accessToken", tokenPair.accessToken(), jwtProperties.getAccessTokenValiditySeconds());
            ResponseCookie refreshCookie = buildCookie("refreshToken", tokenPair.refreshToken(), refreshTokenService.getRefreshTokenValiditySeconds());

            response.addHeader(HttpHeaders.SET_COOKIE, accessCookie.toString());
            response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());

            log.info("토큰 재발행 성공");
            return ResponseEntity.ok().body(ApiResponse.success("토큰 갱신 성공", tokenPair));
        } catch (Exception e) {
            log.warn("토큰 재발행 실패: {}", e.getMessage());
            clearAuthCookies(response);
            throw e;
        }
    }


    @PostMapping("/logout")
    @Operation(summary = "로그아웃", description = "RefreshToken을 삭제하고 쿠키를 무효화합니다.")
    public ResponseEntity<?> logout(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            HttpServletResponse response
    ) {
        authService.logout(userPrincipal.id());
        clearAuthCookies(response);
        return ResponseEntity.ok().body(ApiResponse.success("로그아웃 성공", null));
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

    private String extractCookie(HttpServletRequest request, String name) {
        if (request.getCookies() == null) {
            return null;
        }
        return Arrays.stream(request.getCookies())
                .filter(c -> name.equals(c.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);
    }

    private void clearAuthCookies(HttpServletResponse response) {
        ResponseCookie deleteAccess = buildCookie("accessToken", "", 0);
        ResponseCookie deleteRefresh = buildCookie("refreshToken", "", 0);
        response.addHeader(HttpHeaders.SET_COOKIE, deleteAccess.toString());
        response.addHeader(HttpHeaders.SET_COOKIE, deleteRefresh.toString());
    }

    private ResponseCookie buildCookie(String name, String value, long maxAge) {
        return ResponseCookie.from(name, value)
                .httpOnly(true)
                .secure(cookieSecure)
                .sameSite(cookieSameSite)
                .path("/")
                .maxAge(maxAge)
                .build();
    }
}
