package com.caro.bizkit.observability;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "metrics.http")
public class HttpMetricsFilterProperties {

    private List<String> denyUriExact = List.of();
    private List<String> denyUriPattern = List.of();
    private boolean denyUnknownUri = true;

    public List<String> getDenyUriExact() {
        return denyUriExact.stream().map(String::trim).filter(s -> !s.isBlank()).toList();
    }
    public void setDenyUriExact(List<String> denyUriExact) {
        this.denyUriExact = denyUriExact;
    }

    public List<String> getDenyUriPattern() {
        return denyUriPattern.stream().map(String::trim).filter(s -> !s.isBlank()).toList();
    }
    public void setDenyUriPattern(List<String> denyUriPattern) {
        this.denyUriPattern = denyUriPattern;
    }

    public boolean isDenyUnknownUri() {
        return denyUnknownUri;
    }
    public void setDenyUnknownUri(boolean denyUnknownUri) {
        this.denyUnknownUri = denyUnknownUri;
    }

}