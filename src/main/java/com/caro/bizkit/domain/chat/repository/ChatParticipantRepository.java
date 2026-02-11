package com.caro.bizkit.domain.chat.repository;

import com.caro.bizkit.domain.chat.entity.ChatParticipant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ChatParticipantRepository extends JpaRepository<ChatParticipant, Integer> {

    List<ChatParticipant> findByUserIdAndLeftAtIsNull(Integer userId);

    Optional<ChatParticipant> findByUserIdAndChatRoomId(Integer userId, Integer chatRoomId);

    List<ChatParticipant> findByChatRoomIdAndLeftAtIsNull(Integer chatRoomId);

    boolean existsByUserIdAndChatRoomIdAndLeftAtIsNull(Integer userId, Integer chatRoomId);

    @Query("SELECT cp FROM ChatParticipant cp WHERE cp.chatRoom.id IN " +
            "(SELECT cp2.chatRoom.id FROM ChatParticipant cp2 WHERE cp2.user.id = :userId1) " +
            "AND cp.user.id = :userId2")
    Optional<ChatParticipant> findCommonRoomParticipant(@Param("userId1") Integer userId1,
                                                        @Param("userId2") Integer userId2);
}
