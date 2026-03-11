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
        // edge case: 이미 완료된 태스크 확인
        var latestTask = aiCardTaskRepository.findTopByUser_IdOrderByCreatedAtDesc(userId);
        if (latestTask.isPresent()) {
            AiCardTask task = latestTask.get();
            if (task.getStatus() == AiAnalysisStatus.COMPLETED) {
                SseEmitter immediateEmitter = new SseEmitter(SSE_TIMEOUT);
                try {
                    immediateEmitter.send(SseEmitter.event()
                            .name("completed")
                            .data(Map.of("image_url", task.getResultImageUrl())));
                    immediateEmitter.complete();
                } catch (IOException e) {
                    immediateEmitter.completeWithError(e);
                }
                return immediateEmitter;
            }
            if (task.getStatus() == AiAnalysisStatus.FAILED) {
                SseEmitter immediateEmitter = new SseEmitter(SSE_TIMEOUT);
                try {
                    immediateEmitter.send(SseEmitter.event()
                            .name("failed")
                            .data(Map.of("error", "이미지 생성에 실패했습니다.")));
                    immediateEmitter.complete();
                } catch (IOException e) {
                    immediateEmitter.completeWithError(e);
                }
                return immediateEmitter;
            }
        }

        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT);
        AtomicBoolean cleaned = new AtomicBoolean(false);
        String channel = CHANNEL_PREFIX + userId;

        MessageListener listener = (message, pattern) -> handleRedisMessage(userId, message);

        emitter.onCompletion(() -> {
            if (cleaned.compareAndSet(false, true)) {
                emitterMap.remove(userId);
                listenerContainer.removeMessageListener(listener);
            }
        });

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
        SseEmitter emitter = emitterMap.get(userId);
        if (emitter == null) return;

        try {
            Map<?, ?> data = objectMapper.readValue(message.getBody(), Map.class);
            String event = (String) data.get("event");
            emitter.send(SseEmitter.event().name(event).data(data));
            if ("completed".equals(event) || "failed".equals(event)) {
                emitter.complete();
            }
        } catch (Exception e) {
            log.error("SSE 메시지 전송 실패 userId={}: {}", userId, e.getMessage());
            emitter.completeWithError(e);
        }
    }
}
