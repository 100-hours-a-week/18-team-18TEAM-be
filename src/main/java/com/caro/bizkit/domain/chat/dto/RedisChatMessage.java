package com.caro.bizkit.domain.chat.dto;

import java.time.LocalDateTime;

public record RedisChatMessage(
        Long message_id,
        Integer room_id,
        Integer sender_user_id,
        String sender_name,
        String content,
        LocalDateTime created_at
) {
    public static RedisChatMessage from(ChatMessageResponse response) {
        return new RedisChatMessage(
                response.message_id(),
                response.room_id(),
                response.sender_user_id(),
                response.sender_name(),
                response.content(),
                response.created_at()
        );
    }

    public ChatMessageResponse toMessageResponse() {
        return new ChatMessageResponse(message_id, room_id, sender_user_id, sender_name, content, created_at);
    }
}
