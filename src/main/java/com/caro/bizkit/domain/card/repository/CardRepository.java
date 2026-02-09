package com.caro.bizkit.domain.card.repository;

import com.caro.bizkit.domain.card.entity.Card;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CardRepository extends JpaRepository<Card, Integer> {
    List<Card> findAllByUserId(Integer userId);
    Optional<Card> findByUuid(String uuid);
    Optional<Card> findTopByUserIdOrderByCreatedAtDesc(Integer userId);
    List<Card> findAllByUserIdAndDeletedAtIsNullOrderByStartDateDesc(Integer userId);
    Optional<Card> findTopByUserIdAndDeletedAtIsNullOrderByStartDateDesc(Integer userId);
}
