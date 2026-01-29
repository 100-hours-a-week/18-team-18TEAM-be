package com.caro.bizkit.domain.user.service;

import com.caro.bizkit.common.S3.service.S3Service;
import com.caro.bizkit.domain.auth.entity.Account;
import com.caro.bizkit.domain.auth.entity.OAuth;
import com.caro.bizkit.domain.auth.repository.OAuthRepository;
import com.caro.bizkit.domain.auth.service.KakaoOAuthClient;
import com.caro.bizkit.domain.auth.service.KakaoOAuthProperties;
import com.caro.bizkit.domain.user.dto.UserPrincipal;
import com.caro.bizkit.domain.user.dto.UserResponse;
import java.util.Map;
import java.util.function.Consumer;
import com.caro.bizkit.domain.user.entity.User;
import com.caro.bizkit.domain.user.repository.UserRepository;
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
public class UserService {

    private final S3Service s3Service;
    private final UserRepository userRepository;
    private final OAuthRepository oAuthRepository;
    private final KakaoOAuthClient kakaoOAuthClient;
    private final KakaoOAuthProperties kakaoOAuthProperties;

    public UserResponse getMyStatus(UserPrincipal user) {
        String profileImageUrl = null;
        if (user != null && StringUtils.hasText(user.profile_image_key())) {
            profileImageUrl = s3Service.getPublicUrl(user.profile_image_key());
        }
        return UserResponse.fromPrincipal(user, profileImageUrl);
    }

    @Transactional(readOnly = true)
    public UserResponse getUserProfile(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        if (user.getDeletedAt() != null) {
            return null;
        }
        return toResponse(user);
    }

    @Transactional
    public UserResponse updateMyStatus(UserPrincipal principal, Map<String, Object> request) {
        User user = userRepository.findById(principal.id())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "User not found"));

        if (request == null) {
            return toResponse(user);
        }

        String previousKey = user.getProfileImageKey();
        applyUpdates(user, request);

        if (previousKey != null
                && request.containsKey("profile_image_key")
                && !previousKey.equals(request.get("profile_image_key"))) {
            s3Service.deleteObject(previousKey);
        }

        return toResponse(user);
    }

    private void applyUpdates(User user, Map<String, Object> request) {
        applyIfPresent(request, "name", user::updateName);
        applyIfPresent(request, "phone_number", user::updatePhoneNumber);
        applyIfPresent(request, "lined_number", user::updateLinedNumber);
        applyIfPresent(request, "company", user::updateCompany);
        applyIfPresent(request, "department", user::updateDepartment);
        applyIfPresent(request, "position", user::updatePosition);
        applyIfPresent(request, "profile_image_key", user::updateProfileImageKey);
    }

    private void applyIfPresent(Map<String, Object> request, String key, Consumer<String> updater) {
        if (request.containsKey(key)) {
            updater.accept((String) request.get(key));
        }
    }

    @Transactional
    public void withdraw(UserPrincipal principal) {
        if (principal == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized user");
        }

        User user = userRepository.findById(principal.id())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "User not found"));
        Account account = user.getAccount();

        oAuthRepository.findByAccount(account).ifPresent(oauth -> {
            unlinkFromKakaoIfNeeded(oauth);
            oAuthRepository.delete(oauth);
        });

        account.markDeleted();
        user.markDeleted();
    }

    private void unlinkFromKakaoIfNeeded(OAuth oauth) {
        if (!"kakao".equalsIgnoreCase(oauth.getProvider())) {
            log.warn("Unsupported OAuth provider for unlink: {}", oauth.getProvider());
            return;
        }
        kakaoOAuthClient.unlinkUserByAdminKey(
                kakaoOAuthProperties.getAdminKey(),
                oauth.getProviderId()
        );
    }

    private UserResponse toResponse(User user) {
        String profileImageUrl = null;
        if (StringUtils.hasText(user.getProfileImageKey())) {
            profileImageUrl = s3Service.getPublicUrl(user.getProfileImageKey());
        }
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getPhoneNumber(),
                user.getLinedNumber(),
                user.getCompany(),
                user.getDepartment(),
                user.getPosition(),
                profileImageUrl,
                user.getDescription()
        );
    }
}
