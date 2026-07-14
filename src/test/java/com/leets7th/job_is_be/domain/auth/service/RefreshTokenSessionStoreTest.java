package com.leets7th.job_is_be.domain.auth.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RefreshTokenSessionStoreTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    private RefreshTokenSessionStore sessionStore;

    @BeforeEach
    void setUp() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        sessionStore = new RefreshTokenSessionStore(redisTemplate);
    }

    @Test
    void storesHashInsteadOfRawRefreshTokenAndConsumesOnlyOnce() {
        String rawToken = "raw-refresh-token";
        Duration ttl = Duration.ofDays(14);
        ArgumentCaptor<String> valueCaptor = ArgumentCaptor.forClass(String.class);

        sessionStore.save("session-id", 1L, rawToken, ttl);
        verify(valueOperations).set(
                org.mockito.ArgumentMatchers.eq("auth:refresh:session-id"),
                valueCaptor.capture(),
                org.mockito.ArgumentMatchers.eq(ttl)
        );

        assertFalse(valueCaptor.getValue().contains(rawToken));

        when(valueOperations.getAndDelete("auth:refresh:session-id"))
                .thenReturn(valueCaptor.getValue())
                .thenReturn(null);

        assertTrue(sessionStore.consume("session-id", 1L, rawToken));
        assertFalse(sessionStore.consume("session-id", 1L, rawToken));
    }
}
