package com.caro.bizkit.domain.card.repository;

import com.caro.bizkit.common.baserepository.BaseRepository;
import com.caro.bizkit.domain.card.entity.UserCard;
import java.util.List;
import org.springframework.data.domain.Pageable;

public interface UserCardRepository extends BaseRepository<UserCard, Integer> {
    boolean existsByUserIdAndCardId(Integer userId, Integer cardId);
    List<UserCard> findAllByUserId(Integer userId);
    List<UserCard> findByUserIdOrderByCreatedAtDescIdDesc(Integer userId, Pageable pageable);
    List<UserCard> findByUserIdAndIdLessThanOrderByCreatedAtDescIdDesc(Integer userId, Integer cursorId, Pageable pageable);
}
