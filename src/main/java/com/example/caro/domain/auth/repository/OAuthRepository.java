package com.example.caro.domain.auth.repository;

import com.example.caro.common.baserepository.BaseRepository;
import com.example.caro.domain.auth.entity.OAuth;

import java.util.Optional;

public interface OAuthRepository extends BaseRepository<OAuth, Integer> {
    Optional<OAuth> findByProviderAndProviderId(String provider, String providerId);
}
