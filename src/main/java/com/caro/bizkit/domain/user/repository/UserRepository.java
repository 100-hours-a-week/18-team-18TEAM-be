package com.caro.bizkit.domain.user.repository;

import com.caro.bizkit.common.baserepository.BaseRepository;
import com.caro.bizkit.domain.user.entity.User;
import com.caro.bizkit.domain.auth.entity.Account;

import java.util.Optional;

public interface UserRepository extends BaseRepository<User, Integer> {
    Optional<User> findByAccount(Account account);
}
