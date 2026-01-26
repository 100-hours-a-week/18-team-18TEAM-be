package com.caro.bizkit.domain.card.repository;

import com.caro.bizkit.common.baserepository.BaseRepository;
import com.caro.bizkit.domain.card.entity.UserCard;
import java.util.List;

public interface UserCardRepository extends BaseRepository<UserCard, Integer> {
    boolean existsByUserIdAndCardId(Integer userId, Integer cardId);
    List<UserCard> findAllByUserId(Integer userId);
}
