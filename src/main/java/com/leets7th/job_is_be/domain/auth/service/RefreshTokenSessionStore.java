package com.leets7th.job_is_be.domain.auth.service;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.HexFormat;
import java.util.List;

@Component
public class RefreshTokenSessionStore {

    private static final String KEY_PREFIX = "auth:refresh:";
    private static final String VALUE_SEPARATOR = ":";
    private static final DefaultRedisScript<Long> ROTATE_SCRIPT = new DefaultRedisScript<>("""
            local current = redis.call('GET', KEYS[1])
            if not current or current ~= ARGV[1] then
                return 0
            end
            redis.call('SET', KEYS[2], ARGV[2], 'PX', ARGV[3])
            redis.call('DEL', KEYS[1])
            return 1
            """, Long.class);

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

    public boolean rotate(
            String oldSessionId,
            Long userId,
            String oldRefreshToken,
            String newSessionId,
            String newRefreshToken,
            Duration ttl
    ) {
        Long result = redisTemplate.execute(
                ROTATE_SCRIPT,
                List.of(key(oldSessionId), key(newSessionId)),
                storedValue(userId, oldRefreshToken),
                storedValue(userId, newRefreshToken),
                String.valueOf(ttl.toMillis())
        );
        return Long.valueOf(1L).equals(result);
    }

    private String key(String sessionId) {
        return KEY_PREFIX + sessionId;
    }

    private String storedValue(Long userId, String refreshToken) {
        return userId + VALUE_SEPARATOR + hash(refreshToken);
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
