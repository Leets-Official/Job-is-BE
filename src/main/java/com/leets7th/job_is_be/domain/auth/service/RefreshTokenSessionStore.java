package com.leets7th.job_is_be.domain.auth.service;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.HexFormat;

@Component
public class RefreshTokenSessionStore {

    private static final String KEY_PREFIX = "auth:refresh:";
    private static final String VALUE_SEPARATOR = ":";

    private final StringRedisTemplate redisTemplate;

    public RefreshTokenSessionStore(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void save(String sessionId, Long userId, String refreshToken, Duration ttl) {
        redisTemplate.opsForValue().set(
                key(sessionId),
                userId + VALUE_SEPARATOR + hash(refreshToken),
                ttl
        );
    }

    public boolean consume(String sessionId, Long userId, String refreshToken) {
        String storedValue = redisTemplate.opsForValue().getAndDelete(key(sessionId));
        if (storedValue == null) {
            return false;
        }

        String expectedValue = userId + VALUE_SEPARATOR + hash(refreshToken);
        return MessageDigest.isEqual(
                storedValue.getBytes(StandardCharsets.UTF_8),
                expectedValue.getBytes(StandardCharsets.UTF_8)
        );
    }

    private String key(String sessionId) {
        return KEY_PREFIX + sessionId;
    }

    private String hash(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm is unavailable", e);
        }
    }
}
