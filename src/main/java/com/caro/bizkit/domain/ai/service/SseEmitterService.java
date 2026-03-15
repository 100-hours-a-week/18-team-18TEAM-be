package com.caro.bizkit.domain.ai.service;

import com.caro.bizkit.domain.ai.entity.AiAnalysisStatus;
import com.caro.bizkit.domain.ai.entity.AiCardTask;
import com.caro.bizkit.domain.ai.repository.AiCardTaskRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
@RequiredArgsConstructor
@Slf4j
public class SseEmitterService {

    private static final long SSE_TIMEOUT = 220_000L;
    private static final String CHANNEL_PREFIX = "sse:ai-card:";

    private final ConcurrentHashMap<Integer, SseEmitter> emitterMap = new ConcurrentHashMap<>();
    private final StringRedisTemplate redisTemplate;
    private final RedisMessageListenerContainer listenerContainer;
    private final AiCardTaskRepository aiCardTaskRepository;
    private final ObjectMapper objectMapper;

    public SseEmitter connect(Integer userId) {
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT);
        AtomicBoolean cleaned = new AtomicBoolean(false);
        String channel = CHANNEL_PREFIX + userId;

        MessageListener listener = (message, pattern) -> handleRedisMessage(userId, message);

        emitter.onTimeout(() -> {
            try {
                emitter.send(SseEmitter.event().name("failed").data(Map.of("error", "timeout")));
            } catch (Exception ignored) {}
        });

        SseEmitter existing = emitterMap.put(userId, emitter);
        if (existing != null) {
            existing.complete();
        }
        listenerContainer.addMessageListener(listener, new ChannelTopic(channel));

        emitter.onCompletion(() -> {
            if (cleaned.compareAndSet(false, true)) {
                emitterMap.remove(userId, emitter);
                listenerContainer.removeMessageListener(listener);
            }
        });

        try {
            emitter.send(SseEmitter.event().name("connected").data(Map.of("status", "connected")));
        } catch (IOException e) {
            emitter.completeWithError(e);
        }

        return emitter;
    }

    public void sendProgress(Integer userId, String status, String progress) {
        publish(userId, Map.of("event", "progress", "status", status, "progress", progress != null ? progress : ""));
    }

    public void sendCompleted(Integer userId, String imageUrl) {
        publish(userId, Map.of("event", "completed", "image_url", imageUrl));
    }

    public void sendFailed(Integer userId, String error) {
        publish(userId, Map.of("event", "failed", "error", error));
    }

    private void publish(Integer userId, Map<String, String> data) {
        try {
            String json = objectMapper.writeValueAsString(data);
            redisTemplate.convertAndSend(CHANNEL_PREFIX + userId, json);
        } catch (JsonProcessingException e) {
            log.error("SSE 이벤트 직렬화 실패: {}", e.getMessage());
        }
    }

    private void handleRedisMessage(Integer userId, Message message) {
        log.info("[SSE] Redis 메시지 수신 userId={}, body={}", userId, new String(message.getBody()));
        SseEmitter emitter = emitterMap.get(userId);
        if (emitter == null) {
            log.warn("[SSE] emitter 없음 userId={} — 메시지 무시", userId);
            return;
        }

        try {
            Map<?, ?> data = objectMapper.readValue(message.getBody(), Map.class);
            String event = (String) data.get("event");
            log.info("[SSE] 이벤트 전송 시작 userId={}, event={}", userId, event);
            emitter.send(SseEmitter.event().name(event).data(data));
            log.info("[SSE] 이벤트 전송 완료 userId={}, event={}", userId, event);
            if ("completed".equals(event) || "failed".equals(event)) {
                emitter.complete();
                log.info("[SSE] emitter complete userId={}", userId);
            }
        } catch (Exception e) {
            log.error("[SSE] 메시지 전송 실패 userId={}: {} ({})", userId, e.getMessage(), e.getClass().getSimpleName());
            emitter.completeWithError(e);
        }
    }
}
