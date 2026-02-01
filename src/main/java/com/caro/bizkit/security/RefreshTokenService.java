package com.caro.bizkit.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private static final String KEY_PREFIX = "refresh_token:";

    private final StringRedisTemplate redisTemplate;
    private final JwtProperties jwtProperties;

    public String createRefreshToken(Integer userId) {
        String refreshToken = UUID.randomUUID().toString();
        String hashedToken = hashToken(refreshToken);
        String key = KEY_PREFIX + userId;

        redisTemplate.opsForValue().set(
                key,
                hashedToken,
                jwtProperties.getRefreshTokenValiditySeconds(),
                TimeUnit.SECONDS
        );

        return refreshToken;
    }

    public boolean validateRefreshToken(Integer userId, String refreshToken) {
        String key = KEY_PREFIX + userId;
        String storedHash = redisTemplate.opsForValue().get(key);
        if (storedHash == null) {
            return false;
        }
        String hashedToken = hashToken(refreshToken);
        return storedHash.equals(hashedToken);
    }

    public void deleteRefreshToken(Integer userId) {
        String key = KEY_PREFIX + userId;
        redisTemplate.delete(key);
    }

    public long getRefreshTokenValiditySeconds() {
        return jwtProperties.getRefreshTokenValiditySeconds();
    }

    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }
}
