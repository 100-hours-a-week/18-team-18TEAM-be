package com.caro.bizkit.domain.auth.service;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@Slf4j
@ConfigurationProperties(prefix = "oauth.kakao")
public class KakaoOAuthProperties {
    private String clientId;
    private String clientSecret;
    private List<String> redirectUris = new ArrayList<>();
    private String adminKey;

    public String getRedirectUri(String host) {
        log.info("Selecting redirect_uri for host: {}, available redirectUris: {}", host, redirectUris);

        if (host == null || redirectUris.isEmpty()) {
            String selected = redirectUris.isEmpty() ? null : redirectUris.get(0);
            log.info("Redirect URI selected (fallback): host={}, selected={}", host, selected);
            return selected;
        }

        String hostWithoutPort = host.split(":")[0];
        log.info("Host without port: {}", hostWithoutPort);

        String selected = redirectUris.stream()
                .filter(uri -> uri.contains(host) || uri.contains(hostWithoutPort))
                .findFirst()
                .orElse(redirectUris.get(0));

        boolean isMatched = redirectUris.stream()
                .anyMatch(uri -> uri.contains(host) || uri.contains(hostWithoutPort));

        if (isMatched) {
            log.info("Redirect URI matched: host={}, selected={}", host, selected);
        } else {
            log.warn("No matching redirect URI found for host={}, using default: {}", host, selected);
        }

        return selected;
    }
}
