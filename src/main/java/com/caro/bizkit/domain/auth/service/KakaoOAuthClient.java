package com.caro.bizkit.domain.auth.service;

import com.caro.bizkit.common.exception.CustomException;
import com.caro.bizkit.common.exception.KakaoOAuthException;
import com.caro.bizkit.domain.auth.dto.KakaoOAuthErrorResponse;
import com.caro.bizkit.domain.auth.dto.KakaoTokenResponse;
import com.caro.bizkit.domain.auth.dto.KakaoUnlinkResponse;
import com.caro.bizkit.domain.auth.dto.KakaoUserResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
@Slf4j
public class KakaoOAuthClient {

    private static final String TOKEN_URL = "https://kauth.kakao.com/oauth/token";
    private static final String USER_URL = "https://kapi.kakao.com/v2/user/me";
    private static final String UNLINK_URL = "https://kapi.kakao.com/v1/user/unlink";

    private final KakaoOAuthProperties properties;
    private final WebClient.Builder webClientBuilder;

    public KakaoTokenResponse exchangeCodeForToken(String code, String host) {
        var form = new LinkedMultiValueMap<String, String>();

        String client_id = properties.getClientId();
        String client_secret = properties.getClientSecret();
        String redirect_uri = properties.getRedirectUri(host);

        if (!StringUtils.hasText(client_id) || !StringUtils.hasText(client_secret) || !StringUtils.hasText(redirect_uri)) {
            log.error("Kakao OAuth config missing. clientId={}, redirectUri={}", client_id, redirect_uri);
            throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR, "Kakao OAuth 설정이 누락되었습니다.");
        }

        log.info("Kakao OAuth token exchange - host: {}, redirect_uri: {}", host, redirect_uri);

        form.add("grant_type", "authorization_code");
        form.add("client_id", client_id);
        form.add("client_secret", client_secret);
        form.add("redirect_uri", redirect_uri);
        form.add("code", code);





        return webClientBuilder.build()
                .post()
                .uri(TOKEN_URL)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData(form))
                .retrieve()
                .onStatus(HttpStatusCode::isError, response ->
                        response.bodyToMono(KakaoOAuthErrorResponse.class)
                                .defaultIfEmpty(new KakaoOAuthErrorResponse(null, null, null))
                                .flatMap(error -> Mono.error(toKakaoOAuthException(
                                        "인가 코드가 맞지 않습니다.",
                                        response.statusCode(),
                                        error))))
                .bodyToMono(KakaoTokenResponse.class)
                .block();
    }

    public KakaoUserResponse fetchUser(String accessToken) {
        return webClientBuilder.build()
                .get()
                .uri(USER_URL)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .retrieve()
                .onStatus(HttpStatusCode::isError, response ->
                        response.bodyToMono(KakaoOAuthErrorResponse.class)
                                .defaultIfEmpty(new KakaoOAuthErrorResponse(null, null, null))
                                .flatMap(error -> Mono.error(toKakaoOAuthException(
                                        "카카오 사용자 정보 조회에 실패했습니다.",
                                        response.statusCode(),
                                        error))))
                .bodyToMono(KakaoUserResponse.class)
                .block();
    }

    public KakaoUnlinkResponse unlinkUserByAdminKey(String adminKey, String kakaoUserId) {
        if (!StringUtils.hasText(adminKey)) {
            throw new CustomException(HttpStatus.INTERNAL_SERVER_ERROR, "Kakao Admin Key 설정이 누락되었습니다.");
        }
        if (!StringUtils.hasText(kakaoUserId)) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "Kakao user id가 없습니다.");
        }
        var form = new LinkedMultiValueMap<String, String>();
        form.add("target_id_type", "user_id");
        form.add("target_id", kakaoUserId);

        return webClientBuilder.build()
                .post()
                .uri(UNLINK_URL)
                .header(HttpHeaders.AUTHORIZATION, "KakaoAK " + adminKey)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData(form))
                .retrieve()
                .onStatus(HttpStatusCode::isError, response ->
                        response.bodyToMono(KakaoOAuthErrorResponse.class)
                                .defaultIfEmpty(new KakaoOAuthErrorResponse(null, null, null))
                                .flatMap(error -> Mono.error(toKakaoOAuthException(
                                        "카카오 연결 해제에 실패했습니다.",
                                        response.statusCode(),
                                        error))))
                .bodyToMono(KakaoUnlinkResponse.class)
                .block();
    }

    private KakaoOAuthException toKakaoOAuthException(
            String userMessage,
            HttpStatusCode statusCode,
            KakaoOAuthErrorResponse error
    ) {
        String apiMessage = "Kakao OAuth 요청에 실패했습니다.";
        String errorType = error.error();
        String errorCode = error.errorCode();
        String errorDescription = error.errorDescription();



        if (StringUtils.hasText(errorType) || StringUtils.hasText(errorDescription) || StringUtils.hasText(errorCode)) {
            StringBuilder builder = new StringBuilder("Kakao OAuth error");
            if (StringUtils.hasText(errorType)) {
                builder.append(": ").append(errorType);
            }
            if (StringUtils.hasText(errorCode)) {
                builder.append(" (").append(errorCode).append(")");
            }
            if (StringUtils.hasText(errorDescription)) {
                builder.append(" - ").append(errorDescription);
            }
            apiMessage = builder.toString();
        }

        return new KakaoOAuthException(userMessage, statusCode, apiMessage);
    }
}
