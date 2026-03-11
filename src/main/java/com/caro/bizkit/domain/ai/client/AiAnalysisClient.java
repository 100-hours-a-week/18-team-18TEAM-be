package com.caro.bizkit.domain.ai.client;

import com.caro.bizkit.common.exception.CustomException;
import com.caro.bizkit.domain.ai.config.AiClientProperties;
import com.caro.bizkit.domain.ai.dto.AiCardGenerateRequest;
import com.caro.bizkit.domain.ai.dto.AiCardGenerateResponse;
import com.caro.bizkit.domain.ai.dto.AiHexAnalyzeRequest;
import com.caro.bizkit.domain.ai.dto.AiHexAnalyzeResponse;
import com.caro.bizkit.domain.ai.dto.AiJobAnalyzeRequest;
import com.caro.bizkit.domain.ai.dto.AiJobAnalyzeResponse;
import com.caro.bizkit.domain.ai.dto.AiJobSubmitResponse;
import com.caro.bizkit.domain.ai.dto.AiTaskStatusResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Optional;

@Component
@Slf4j
public class AiAnalysisClient {

    private final RestClient restClient;

    public AiAnalysisClient(RestClient.Builder restClientBuilder, AiClientProperties properties) {
        this.restClient = restClientBuilder.baseUrl(properties.getBaseUrl()).build();
    }

    public AiJobSubmitResponse submitAnalysis(AiJobAnalyzeRequest request) {
        log.info("Card {} AI 분석 요청", request.cardId());
        return restClient.post()
                .uri("/ai/job/analyze")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .exchange((req, response) -> {
                    if (response.getStatusCode().isError()) {
                        log.error("AI 서버 오류: {}", response.getStatusCode());
                        throw new CustomException(response.getStatusCode(), "AI 서버 오류: " + response.getStatusCode());
                    }
                    return response.bodyTo(AiJobSubmitResponse.class);
                });
    }

    public AiJobSubmitResponse submitHexAnalysis(AiHexAnalyzeRequest request) {
        log.info("User {} 6각 차트 분석 요청", request.userId());
        return restClient.post()
                .uri("/ai/hex/analyze")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .exchange((req, response) -> {
                    if (response.getStatusCode().isError()) {
                        log.error("AI 서버 오류: {}", response.getStatusCode());
                        throw new CustomException(response.getStatusCode(), "AI 서버 오류: " + response.getStatusCode());
                    }
                    return response.bodyTo(AiJobSubmitResponse.class);
                });
    }

    public AiTaskStatusResponse getTaskStatus(String taskId) {
        return restClient.get()
                .uri("/ai/tasks/{taskId}", taskId)
                .exchange((req, response) -> {
                    if (response.getStatusCode().isError()) {
                        throw new CustomException(response.getStatusCode(), "AI 상태 조회 오류: " + response.getStatusCode());
                    }
                    return response.bodyTo(AiTaskStatusResponse.class);
                });
    }

    public Optional<AiJobAnalyzeResponse> getTaskResult(String taskId) {
        return restClient.get()
                .uri("/ai/tasks/{taskId}/result", taskId)
                .exchange((req, response) -> {
                    int status = response.getStatusCode().value();
                    if (status == 200) return Optional.ofNullable(response.bodyTo(AiJobAnalyzeResponse.class));
                    else if (status == 202) return Optional.<AiJobAnalyzeResponse>empty();
                    else throw new CustomException(response.getStatusCode(), "AI 결과 조회 실패: " + status);
                });
    }

    public AiJobSubmitResponse submitCardGeneration(AiCardGenerateRequest request) {
        log.info("User {} AI 명함 이미지 생성 요청", request.userId());
        return restClient.post()
                .uri("/ai/card/generate")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .exchange((req, response) -> {
                    if (response.getStatusCode().isError()) {
                        log.error("AI 서버 오류: {}", response.getStatusCode());
                        throw new CustomException(response.getStatusCode(), "AI 서버 오류: " + response.getStatusCode());
                    }
                    return response.bodyTo(AiJobSubmitResponse.class);
                });
    }

    public Optional<AiCardGenerateResponse> getCardTaskResult(String taskId) {
        return restClient.get()
                .uri("/ai/tasks/{taskId}/result", taskId)
                .exchange((req, response) -> {
                    int status = response.getStatusCode().value();
                    if (status == 200) return Optional.ofNullable(response.bodyTo(AiCardGenerateResponse.class));
                    else if (status == 202) return Optional.<AiCardGenerateResponse>empty();
                    else throw new CustomException(response.getStatusCode(), "AI 결과 조회 실패: " + status);
                });
    }

    public Optional<AiHexAnalyzeResponse> getHexTaskResult(String taskId) {
        return restClient.get()
                .uri("/ai/tasks/{taskId}/result", taskId)
                .exchange((req, response) -> {
                    int status = response.getStatusCode().value();
                    if (status == 200) return Optional.ofNullable(response.bodyTo(AiHexAnalyzeResponse.class));
                    else if (status == 202) return Optional.<AiHexAnalyzeResponse>empty();
                    else throw new CustomException(response.getStatusCode(), "AI 결과 조회 실패: " + status);
                });
    }
}
