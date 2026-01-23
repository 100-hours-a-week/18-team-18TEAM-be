package com.caro.bizkit.domain.user.service;

import com.caro.bizkit.common.S3.service.S3Service;
import com.caro.bizkit.domain.user.dto.UserPrincipal;
import com.caro.bizkit.domain.user.dto.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class UserService {

    private final S3Service s3Service;

    public UserResponse getMyStatus(UserPrincipal user) {
        String profileImageUrl = null;
        if (user != null && StringUtils.hasText(user.profile_image_key())) {
            profileImageUrl = s3Service.createReadUrl(user.profile_image_key()).url();
        }
        return UserResponse.fromPrincipal(user, profileImageUrl);
    }
}
