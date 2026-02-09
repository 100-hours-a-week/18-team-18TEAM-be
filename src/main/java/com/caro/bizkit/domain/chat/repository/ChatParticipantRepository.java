package com.caro.bizkit.domain.chat.repository;

import com.caro.bizkit.domain.chat.entity.ChatParticipant;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatParticipantRepository extends JpaRepository<ChatParticipant, Integer> {
}
