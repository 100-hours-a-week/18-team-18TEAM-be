package com.caro.bizkit.observability;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "metrics.http")
public class HttpMetricsFilterProperties {

    private List<String> denyUriExact = List.of();

    public List<String> getDenyUriExact() {
        return denyUriExact.stream().map(String::trim).filter(s -> !s.isBlank()).toList();
    }
    public void setDenyUriExact(List<String> denyUriExact) { this.denyUriExact = denyUriExact; }

}