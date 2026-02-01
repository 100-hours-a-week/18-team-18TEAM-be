package com.caro.bizkit.domain.ai.client;

import com.caro.bizkit.domain.ai.config.AiClientProperties;
import com.caro.bizkit.domain.ai.dto.AiJobAnalyzeRequest;
import com.caro.bizkit.domain.ai.dto.AiJobAnalyzeResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;

@Component
@RequiredArgsConstructor
@Slf4j
public class AiAnalysisClient {

    private final WebClient.Builder webClientBuilder;
    private final AiClientProperties properties;

    public AiJobAnalyzeResponse analyzeSync(AiJobAnalyzeRequest request) {
        log.info("Requesting AI analysis for user: {}", request.userId());

        return webClientBuilder.build()
                .post()
                .uri(properties.getBaseUrl() + "/ai/job/analyze/sync")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(AiJobAnalyzeResponse.class)
                .block(Duration.ofSeconds(properties.getTimeoutSeconds()));
    }
}
