package com.caro.bizkit.domain.auth.service;

import com.caro.bizkit.domain.auth.dto.KakaoTokenResponse;
import com.caro.bizkit.domain.auth.dto.KakaoUserResponse;
import com.caro.bizkit.domain.auth.entity.Account;
import com.caro.bizkit.domain.auth.entity.OAuth;
import com.caro.bizkit.domain.auth.repository.AccountRepository;
import com.caro.bizkit.domain.auth.repository.OAuthRepository;
import com.caro.bizkit.domain.auth.dto.AccessTokenResponse;
import com.caro.bizkit.domain.user.entity.AiUsage;
import com.caro.bizkit.domain.user.entity.User;
import com.caro.bizkit.domain.user.repository.AiUsageRepository;
import com.caro.bizkit.domain.user.repository.UserRepository;
import com.caro.bizkit.security.JwtTokenProvider;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final KakaoOAuthClient kakaoOAuthClient;
    private final KakaoOAuthProperties kakaoOAuthProperties;
    private final AccountRepository accountRepository;
    private final OAuthRepository oAuthRepository;
    private final UserRepository userRepository;
    private final AiUsageRepository aiUsageRepository;
    private final JwtTokenProvider jwtTokenProvider;


    @Transactional
    public AccessTokenResponse login(String provider, String code, String redirectUri) {
        if (!"kakao".equalsIgnoreCase(provider)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unsupported provider: " + provider);
        }
        KakaoTokenResponse tokenResponse = kakaoOAuthClient.exchangeCodeForToken(code, redirectUri);
        if (tokenResponse == null || !StringUtils.hasText(tokenResponse.accessToken())) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Failed to get access token");
        }


        KakaoUserResponse userResponse = kakaoOAuthClient.fetchUser(tokenResponse.accessToken());
        if (userResponse == null || userResponse.id() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Failed to fetch Kakao user profile");
        }
        KakaoUserResponse.KakaoAccount kakaoAccount = userResponse.kakaoAccount();
        KakaoUserResponse.KakaoProfile kakaoProfile = kakaoAccount.profile();

        String email = kakaoAccount.email();
        String nickname = kakaoProfile.nickname();


        Account account = oAuthRepository.findByProviderAndProviderId(provider, String.valueOf(userResponse.id()))
                .map(OAuth::getAccount)
                .orElseGet(() -> signUpAccount(provider, String.valueOf(userResponse.id()), email, nickname));

        account.updateLoggedAt(java.time.LocalDateTime.now());

        User user = userRepository.findByAccount(account)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "User not found"));

        String token = jwtTokenProvider.generateAccessToken(
                String.valueOf(user.getId()),
                Map.of()
        );
        log.info("logged in with {}", account.getLoginEmail());
        return new AccessTokenResponse(token);
    }

    @Transactional
    public Account signUpAccount(String provider, String providerId, String loginEmail, String nickname) {
        Account account = Account.create(loginEmail);
        Account savedAccount = accountRepository.save(account);

        OAuth oauth = OAuth.create(savedAccount, provider, providerId);
        oAuthRepository.save(oauth);

        User user = User.create(savedAccount, nickname, loginEmail);
        AiUsage aiUsage = AiUsage.create(user);
        userRepository.save(user);
        aiUsageRepository.save(aiUsage);
        log.info("Account created: {}", savedAccount);

        return savedAccount;
    }
}
