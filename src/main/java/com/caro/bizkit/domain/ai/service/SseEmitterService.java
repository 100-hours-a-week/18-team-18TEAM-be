package com.caro.bizkit.domain.ai.service;

import org.springframework.stereotype.Service;

@Service
public class SseEmitterService {

    public void sendProgress(Integer userId, String status, String progress) {
        // TODO: implement
    }

    public void sendCompleted(Integer userId, String imageUrl) {
        // TODO: implement
    }

    public void sendFailed(Integer userId, String error) {
        // TODO: implement
    }
}
