package com.caro.bizkit.domain.card.repository;

import com.caro.bizkit.common.baserepository.BaseRepository;
import com.caro.bizkit.domain.card.entity.UserCard;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserCardRepository extends BaseRepository<UserCard, Integer> {
    boolean existsByUserIdAndCardId(Integer userId, Integer cardId);
    List<UserCard> findAllByUserId(Integer userId);
    Optional<UserCard> findByUserIdAndCardId(Integer userId, Integer cardId);
    List<UserCard> findByUserIdOrderByCreatedAtDescIdDesc(Integer userId, Pageable pageable);
    List<UserCard> findByUserIdAndIdLessThanOrderByCreatedAtDescIdDesc(Integer userId, Integer cursorId, Pageable pageable);

    @Query(value = """
            select uc.* from user_card uc
            join card c on uc.card_id = c.id
            where uc.user_id = :userId
              and (:cursorId is null or uc.id < :cursorId)
              and match(c.name, c.company, c.email, c.position)
                  against (concat(:keyword, '*') in boolean mode)
            order by uc.created_at desc, uc.id desc
            """, nativeQuery = true)
    List<UserCard> searchCollectedCards(
            @Param("userId") Integer userId,
            @Param("cursorId") Integer cursorId,
            @Param("keyword") String keyword,
            Pageable pageable
    );
}
