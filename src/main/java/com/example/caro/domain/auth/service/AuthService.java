package com.example.caro.domain.auth.service;

import com.example.caro.domain.auth.dto.KakaoTokenResponse;
import com.example.caro.domain.auth.dto.KakaoUserResponse;
import com.example.caro.domain.auth.entity.Account;
import com.example.caro.domain.auth.entity.OAuth;
import com.example.caro.domain.auth.repository.AccountRepository;
import com.example.caro.domain.auth.repository.OAuthRepository;
import com.example.caro.domain.user.dto.UserResponse;
import com.example.caro.domain.user.entity.AiUsage;
import com.example.caro.domain.user.entity.User;
import com.example.caro.domain.user.repository.AiUsageRepository;
import com.example.caro.domain.user.repository.UserRepository;
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


    @Transactional
    public UserResponse login(String provider, String code) {
        if (!"kakao".equalsIgnoreCase(provider)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unsupported provider: " + provider);
        }
        KakaoTokenResponse tokenResponse = kakaoOAuthClient.exchangeCodeForToken(code);
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

        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getPhoneNumber(),
                user.getLinedNumber(),
                user.getCompany(),
                user.getDepartment(),
                user.getPosition(),
                null,
                user.getDescription()
        );
    }

    @Transactional
    public Account signUpAccount(String provider, String providerId, String loginEmail, String nickname) {
        Account account = Account.create(loginEmail);
        Account savedAccount = accountRepository.save(account);

        OAuth oauth = OAuth.create(savedAccount, provider, providerId);
        oAuthRepository.save(oauth);

        User user = User.create(savedAccount, nickname, loginEmail);
        AiUsage aiUsage = AiUsage.create();
        user.attachAiUsage(aiUsage);
        userRepository.save(user);
        aiUsageRepository.save(aiUsage);
        log.info("Account created: {}", savedAccount);

        return savedAccount;
    }
}
