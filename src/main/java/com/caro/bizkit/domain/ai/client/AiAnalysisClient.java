package com.caro.bizkit.domain.ai.client;

import com.caro.bizkit.common.exception.CustomException;
import com.caro.bizkit.domain.ai.config.AiClientProperties;
import com.caro.bizkit.domain.ai.dto.AiJobAnalyzeRequest;
import com.caro.bizkit.domain.ai.dto.AiJobAnalyzeResponse;
import com.caro.bizkit.domain.ai.dto.AiJobSubmitResponse;
import com.caro.bizkit.domain.ai.dto.AiTaskStatusResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class AiAnalysisClient {

    private final WebClient.Builder webClientBuilder;
    private final AiClientProperties properties;

    public AiJobSubmitResponse submitAnalysis(AiJobAnalyzeRequest request) {
        log.info("Card {} AI 분석 요청", request.cardId());
        return webClientBuilder.build()
                .post()
                .uri(properties.getBaseUrl() + "/ai/job/analyze")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .retrieve()
                .onStatus(HttpStatusCode::isError, response -> {
                    log.error("AI 서버 오류: {}", response.statusCode());
                    return Mono.error(new CustomException(response.statusCode(), "AI 서버 오류: " + response.statusCode()));
                })
                .bodyToMono(AiJobSubmitResponse.class)
                .block(Duration.ofSeconds(properties.getTimeoutSeconds()));
    }

    public AiTaskStatusResponse getTaskStatus(String taskId) {
        return webClientBuilder.build()
                .get()
                .uri(properties.getBaseUrl() + "/ai/tasks/{taskId}", taskId)
                .retrieve()
                .onStatus(HttpStatusCode::isError, response ->
                        Mono.error(new CustomException(response.statusCode(), "AI 상태 조회 오류: " + response.statusCode())))
                .bodyToMono(AiTaskStatusResponse.class)
                .block(Duration.ofSeconds(properties.getTimeoutSeconds()));
    }

    public Optional<AiJobAnalyzeResponse> getTaskResult(String taskId) {
        return webClientBuilder.build()
                .get()
                .uri(properties.getBaseUrl() + "/ai/tasks/{taskId}/result", taskId)
                .exchangeToMono(response -> {
                    int status = response.statusCode().value();
                    if (status == 200) {
                        return response.bodyToMono(AiJobAnalyzeResponse.class).map(Optional::of);
                    } else if (status == 202) {
                        return Mono.just(Optional.<AiJobAnalyzeResponse>empty());
                    } else {
                        return Mono.error(new CustomException(response.statusCode(), "AI 결과 조회 실패: " + status));
                    }
                })
                .block(Duration.ofSeconds(properties.getTimeoutSeconds()));
    }
}
