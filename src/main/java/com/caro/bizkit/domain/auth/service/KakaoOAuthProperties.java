package com.caro.bizkit.domain.auth.service;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "oauth.kakao")
public class KakaoOAuthProperties {
    private String clientId;
    private String clientSecret;
    private List<String> redirectUris = new ArrayList<>();
    private String adminKey;

    public String getRedirectUri(String host) {
        if (host == null || redirectUris.isEmpty()) {
            return redirectUris.isEmpty() ? null : redirectUris.get(0);
        }
        String hostWithoutPort = host.split(":")[0];
        return redirectUris.stream()
                .filter(uri -> uri.contains(host) || uri.contains(hostWithoutPort))
                .findFirst()
                .orElse(redirectUris.get(0));
    }
}
