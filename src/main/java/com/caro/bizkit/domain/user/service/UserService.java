package com.caro.bizkit.domain.user.service;

import com.caro.bizkit.common.S3.service.S3Service;
import com.caro.bizkit.domain.auth.entity.Account;
import com.caro.bizkit.domain.auth.entity.OAuth;
import com.caro.bizkit.domain.auth.repository.AccountRepository;
import com.caro.bizkit.domain.auth.repository.OAuthRepository;
import com.caro.bizkit.domain.auth.service.KakaoOAuthClient;
import com.caro.bizkit.domain.auth.service.KakaoOAuthProperties;
import com.caro.bizkit.domain.user.dto.UserPrincipal;
import com.caro.bizkit.domain.user.dto.UserRequest;
import com.caro.bizkit.domain.user.dto.UserResponse;
import com.caro.bizkit.domain.user.entity.User;
import com.caro.bizkit.domain.user.repository.UserRepository;
import com.caro.bizkit.domain.withdrawl.entity.AccountWithdrawal;
import com.caro.bizkit.domain.withdrawl.entity.Withdrawal;
import com.caro.bizkit.domain.withdrawl.repository.AccountWithdrawalRepository;
import com.caro.bizkit.domain.withdrawl.repository.WithdrawalRepository;
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
    private final WithdrawalRepository withdrawalRepository;
    private final AccountWithdrawalRepository accountWithdrawalRepository;
    private final KakaoOAuthClient kakaoOAuthClient;
    private final KakaoOAuthProperties kakaoOAuthProperties;

    public UserResponse getMyStatus(UserPrincipal user) {
        String profileImageUrl = null;
        if (user != null && StringUtils.hasText(user.profile_image_key())) {
            profileImageUrl = s3Service.createReadUrl(user.profile_image_key()).url();
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
    public UserResponse updateMyStatus(UserPrincipal principal, UserRequest request) {
        if (principal == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized user");
        }
        User user = userRepository.findById(principal.id())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "User not found"));
        if (request != null) {
            applyUpdates(user, request);
        }
        return toResponse(user);
    }

    private void applyUpdates(User user, UserRequest request) {
        if (request.name() != null) {
            user.updateName(request.name());
        }
        if (request.phone_number() != null) {
            user.updatePhoneNumber(request.phone_number());
        }
        if (request.lined_number() != null) {
            user.updateLinedNumber(request.lined_number());
        }
        if (request.company() != null) {
            user.updateCompany(request.company());
        }
        if (request.department() != null) {
            user.updateDepartment(request.department());
        }
        if (request.position() != null) {
            user.updatePosition(request.position());
        }
        if (request.profile_image_key() != null) {
            user.updateProfileImageKey(request.profile_image_key());
        }
        if (request.description() != null) {
            user.updateDescription(request.description());
        }
    }

    @Transactional
    public void withdraw(UserPrincipal principal, Integer reasonId) {
        if (principal == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized user");
        }

        User user = userRepository.findById(principal.id())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "User not found"));
        Account account = user.getAccount();



        Withdrawal withdrawal = withdrawalRepository.findById(reasonId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid reason id"));

        AccountWithdrawal accountWithdrawal = AccountWithdrawal.create(account, withdrawal);
        accountWithdrawalRepository.save(accountWithdrawal);




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
            profileImageUrl = s3Service.createReadUrl(user.getProfileImageKey()).url();
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
