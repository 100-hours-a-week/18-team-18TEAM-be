package com.caro.bizkit.domain.chat.service;

import com.caro.bizkit.domain.chat.dto.RedisChatMessage;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChatRedisPublisher {

    private final StringRedisTemplate redisTemplate;
    private final ChannelTopic chatMessageTopic;
    private final ObjectMapper objectMapper;

    public void publish(RedisChatMessage message) {
        try {
            String json = objectMapper.writeValueAsString(message);
            redisTemplate.convertAndSend(chatMessageTopic.getTopic(), json);
        } catch (JsonProcessingException e) {
            log.error("Redis publish 직렬화 실패: roomId={}, messageId={}", message.room_id(), message.message_id(), e);
        }
    }
}
