package com.caro.bizkit.domain.card.repository;

import com.caro.bizkit.common.baserepository.BaseRepository;
import com.caro.bizkit.domain.card.entity.Card;
import java.util.List;

public interface CardRepository extends BaseRepository<Card, Integer> {
    List<Card> findAllByUserId(Integer userId);
}
