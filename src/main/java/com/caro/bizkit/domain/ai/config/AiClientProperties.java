package com.caro.bizkit.domain.ai.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "ai.client")
public class AiClientProperties {
    private String baseUrl;
    private int timeoutSeconds;
}
