package com.caro.bizkit.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class RefreshTokenService {

    private static final String USER_KEY_PREFIX = "refresh_token:user:";
    private static final String LOOKUP_KEY_PREFIX = "refresh_token:lookup:";

    private final StringRedisTemplate redisTemplate;
    private final JwtProperties jwtProperties;

    public String createRefreshToken(Integer userId) {
        log.info("[RefreshToken 생성] userId={}", userId);
        deleteRefreshToken(userId);

        String refreshToken = UUID.randomUUID().toString();
        String hashedToken = hashToken(refreshToken);
        long ttl = jwtProperties.getRefreshTokenValiditySeconds();
        log.info("[RefreshToken 생성] 생성된 UUID={}, hashedToken={}", refreshToken, hashedToken);

        String userKey = USER_KEY_PREFIX + userId;
        redisTemplate.opsForValue().set(userKey, hashedToken, ttl, TimeUnit.SECONDS);
        log.info("[RefreshToken 생성] Redis 저장 - userKey={}, hashedToken={}", userKey, hashedToken);

        String lookupKey = LOOKUP_KEY_PREFIX + hashedToken;
        redisTemplate.opsForValue().set(lookupKey, String.valueOf(userId), ttl, TimeUnit.SECONDS);
        log.info("[RefreshToken 생성] Redis 저장 - lookupKey={}, userId={}, ttl={}초", lookupKey, userId, ttl);

        return refreshToken;
    }

    public Integer validateAndGetUserId(String refreshToken) {
        log.info("[RefreshToken 검증] 시작 - refreshToken={}", refreshToken);

        String hashedToken = hashToken(refreshToken);
        String lookupKey = LOOKUP_KEY_PREFIX + hashedToken;
        log.info("[RefreshToken 검증] lookupKey={}", lookupKey);

        String userIdStr = redisTemplate.opsForValue().get(lookupKey);
        log.info("[RefreshToken 검증] Redis에서 조회한 userIdStr={}", userIdStr);

        if (userIdStr == null) {
            log.warn("[RefreshToken 검증] 실패 - lookupKey에서 userId를 찾을 수 없음");
            return null;
        }

        Integer userId = Integer.valueOf(userIdStr);
        String userKey = USER_KEY_PREFIX + userId;
        String storedHash = redisTemplate.opsForValue().get(userKey);
        log.info("[RefreshToken 검증] userKey={}, storedHash={}", userKey, storedHash);

        if (storedHash == null || !storedHash.equals(hashedToken)) {
            log.warn("[RefreshToken 검증] 실패 - 해시 불일치 또는 userKey 없음. storedHash={}, hashedToken={}", storedHash, hashedToken);
            return null;
        }

        log.info("[RefreshToken 검증] 성공 - userId={}", userId);
        return userId;
    }

    public void deleteRefreshToken(Integer userId) {
        String userKey = USER_KEY_PREFIX + userId;
        String storedHash = redisTemplate.opsForValue().get(userKey);

        if (storedHash != null) {
            String lookupKey = LOOKUP_KEY_PREFIX + storedHash;
            redisTemplate.delete(lookupKey);
        }
        redisTemplate.delete(userKey);
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
