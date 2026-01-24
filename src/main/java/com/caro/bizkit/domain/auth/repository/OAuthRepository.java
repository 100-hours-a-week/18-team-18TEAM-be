package com.caro.bizkit.domain.auth.repository;

import com.caro.bizkit.common.baserepository.BaseRepository;
import com.caro.bizkit.domain.auth.entity.OAuth;
import com.caro.bizkit.domain.auth.entity.Account;

import java.util.Optional;

public interface OAuthRepository extends BaseRepository<OAuth, Integer> {
    Optional<OAuth> findByProviderAndProviderId(String provider, String providerId);
    Optional<OAuth> findByAccount(Account account);
}
