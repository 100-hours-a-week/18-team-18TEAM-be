package com.example.caro.domain.auth.service;

import com.example.caro.domain.auth.dto.KakaoTokenResponse;
import com.example.caro.domain.auth.dto.KakaoUserResponse;
import com.example.caro.domain.auth.dto.LoginResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final KakaoOAuthClient kakaoOAuthClient;
    private final KakaoOAuthProperties kakaoOAuthProperties;

    public LoginResponse login(String provider, String code) {
        if (!"kakao".equalsIgnoreCase(provider)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unsupported provider: " + provider);
        }

        validateKakaoConfig();
        System.out.println("test");

        KakaoTokenResponse tokenResponse = kakaoOAuthClient.exchangeCodeForToken(code);
        if (tokenResponse == null || !StringUtils.hasText(tokenResponse.accessToken())) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Failed to get Kakao access token");
        }


        KakaoUserResponse userResponse = kakaoOAuthClient.fetchUser(tokenResponse.accessToken());
        if (userResponse == null || userResponse.id() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Failed to fetch Kakao user profile");
        }

        String email = userResponse.kakaoAccount() != null ? userResponse.kakaoAccount().email() : null;
        String nickname = userResponse.properties() != null ? userResponse.properties().nickname() : null;
        String imageUrl = null;
        if (userResponse.kakaoAccount() != null && userResponse.kakaoAccount().profile() != null) {
            imageUrl = userResponse.kakaoAccount().profile().profileImageUrl();
        }
        if (!StringUtils.hasText(imageUrl) && userResponse.properties() != null) {
            imageUrl = userResponse.properties().profileImage();
        }

        return new LoginResponse(
                "kakao",
                String.valueOf(userResponse.id()),
                email,
                nickname,
                imageUrl
        );
    }

    private void validateKakaoConfig() {
        if (!StringUtils.hasText(kakaoOAuthProperties.getClientId())) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Kakao client id is missing");
        }
    }
}
