package com.caro.bizkit.domain.auth.repository;

import com.caro.bizkit.common.baserepository.BaseRepository;
import com.caro.bizkit.domain.auth.entity.OAuth;

import java.util.Optional;

public interface OAuthRepository extends BaseRepository<OAuth, Integer> {
    Optional<OAuth> findByProviderAndProviderId(String provider, String providerId);
}
