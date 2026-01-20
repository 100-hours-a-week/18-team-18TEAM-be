package com.example.caro.common.exception;

import lombok.Getter;
import org.apache.kafka.shaded.io.opentelemetry.proto.trace.v1.Status;
import org.springframework.http.HttpStatusCode;

@Getter
public class KakaoOAuthException extends RuntimeException {
    private final HttpStatusCode status;
    private final String apiMessage;

    public KakaoOAuthException(String message, HttpStatusCode status,  String apiMessage) {

        super(message);
        this.status = status;
        this.apiMessage = apiMessage;
    }
}
