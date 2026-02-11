package com.caro.bizkit.domain.chat.service;

import com.caro.bizkit.domain.chat.dto.ChatNotification;
import com.caro.bizkit.domain.chat.dto.RedisChatMessage;
import com.caro.bizkit.domain.chat.entity.ChatParticipant;
import com.caro.bizkit.domain.chat.repository.ChatMessageRepository;
import com.caro.bizkit.domain.chat.repository.ChatParticipantRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChatRedisSubscriber {

    private final SimpMessagingTemplate messagingTemplate;
    private final ChatParticipantRepository chatParticipantRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ObjectMapper objectMapper;

    public void onMessage(String message) {
        try {
            RedisChatMessage redisMessage = objectMapper.readValue(message, RedisChatMessage.class);

            // 1. 채팅방 구독자에게 메시지 전달
            messagingTemplate.convertAndSend(
                    "/sub/chat/rooms/" + redisMessage.room_id(),
                    redisMessage.toMessageResponse()
            );

            // 2. 방의 활성 참여자에게 개인 알림 (발신자 제외)
            List<ChatParticipant> participants = chatParticipantRepository
                    .findByChatRoomIdAndLeftAtIsNull(redisMessage.room_id());

            for (ChatParticipant participant : participants) {
                Integer userId = participant.getUser().getId();
                if (userId.equals(redisMessage.sender_user_id())) {
                    continue;
                }

                int unreadCount = chatMessageRepository.countUnreadMessages(
                        redisMessage.room_id(),
                        participant.getLastReadMessageId(),
                        participant.getJoinedAt(),
                        userId
                );

                ChatNotification notification = new ChatNotification(
                        redisMessage.room_id(),
                        unreadCount,
                        redisMessage.content(),
                        redisMessage.created_at()
                );

                messagingTemplate.convertAndSendToUser(
                        String.valueOf(userId),
                        "/queue/chat/notifications",
                        notification
                );
            }
        } catch (Exception e) {
            log.error("Redis 메시지 처리 실패", e);
        }
    }
}
