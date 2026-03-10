package com.caro.bizkit.domain.ai.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "ai.client")
public class AiClientProperties {
    private String baseUrl;
    private Job job = new Job();
    private Hex hex = new Hex();

    @Getter
    @Setter
    public static class Job {
        private int timeoutSeconds;
        private int pollIntervalSeconds;
    }

    @Getter
    @Setter
    public static class Hex {
        private int timeoutSeconds;
        private int pollIntervalSeconds;
    }
}
