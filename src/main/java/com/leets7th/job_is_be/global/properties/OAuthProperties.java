package com.leets7th.job_is_be.global.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.net.URI;
import java.time.Duration;
import java.util.List;

@ConfigurationProperties(prefix = "app.oauth")
public record OAuthProperties(
        URI frontendSuccessUri,
        URI frontendFailureUri,
        Duration stateTtl,
        Provider kakao,
        Provider google
) {

    public OAuthProperties {
        if (stateTtl == null || stateTtl.isNegative() || stateTtl.isZero()) {
            throw new IllegalArgumentException("OAuth state TTL must be positive");
        }
    }

    public Provider getProvider(String provider) {
        return switch (provider.toLowerCase()) {
            case "kakao" -> kakao;
            case "google" -> google;
            default -> throw new IllegalArgumentException("Unsupported OAuth provider: " + provider);
        };
    }

    public record Provider(
            String clientId,
            String clientSecret,
            URI redirectUri,
            URI authorizationUri,
            URI tokenUri,
            URI userInfoUri,
            List<String> scopes
    ) {

        public boolean isConfigured() {
            return clientId != null && !clientId.isBlank()
                    && clientSecret != null && !clientSecret.isBlank();
        }
    }
}
