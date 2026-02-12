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
              and match(c.name, c.company, c.email, c.position, c.phone_number, c.department)
                  against (:keyword in boolean mode)
            order by uc.created_at desc, uc.id desc
            """, nativeQuery = true)
    List<UserCard> searchCollectedCards(
            @Param("userId") Integer userId,
            @Param("keyword") String keyword,
            Pageable pageable
    );

    @Query(value = """
            select uc.* from user_card uc
            join card c on uc.card_id = c.id
            where uc.user_id = :userId
              and uc.id < :cursorId
              and match(c.name, c.company, c.email, c.position, c.phone_number, c.department)
                  against (:keyword in boolean mode)
            order by uc.created_at desc, uc.id desc
            """, nativeQuery = true)
    List<UserCard> searchCollectedCardsWithCursor(
            @Param("userId") Integer userId,
            @Param("cursorId") Integer cursorId,
            @Param("keyword") String keyword,
            Pageable pageable
    );

    void deleteAllByUserId(Integer userId);

    /**
     * 특정 사용자(collectorId)가 수집한 카드 중에서
     * 카드 주인이 cardOwnerId인 카드가 있는지 확인
     */
    @Query("""
            SELECT CASE WHEN COUNT(uc) > 0 THEN true ELSE false END
            FROM UserCard uc
            WHERE uc.user.id = :collectorId
              AND uc.card.user.id = :cardOwnerId
            """)
    boolean existsCollectedCardByOwner(
            @Param("collectorId") Integer collectorId,
            @Param("cardOwnerId") Integer cardOwnerId
    );
}
