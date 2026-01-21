package com.example.caro.domain.user.repository;

import com.example.caro.common.baserepository.BaseRepository;
import com.example.caro.domain.user.entity.User;
import com.example.caro.domain.auth.entity.Account;

import java.util.Optional;

public interface UserRepository extends BaseRepository<User, Integer> {
    Optional<User> findByAccount(Account account);
}
