package com.caro.bizkit.domain.card.repository;

import com.caro.bizkit.common.baserepository.BaseRepository;
import com.caro.bizkit.domain.card.entity.Card;
import java.util.List;
import java.util.Optional;

public interface CardRepository extends BaseRepository<Card, Integer> {
    List<Card> findAllByUserId(Integer userId);
    Optional<Card> findByUuid(String uuid);
}
