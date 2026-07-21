package com.leets7th.job_is_be.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.auditing.DateTimeProvider;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.time.OffsetDateTime;
import java.util.Optional;

// BaseEntity의 @CreatedDate, @LastModifiedDate 활성화
// BaseEntity의 필드 타입이 OffsetDateTime이라, 기본 LocalDateTime 프로바이더 대신 OffsetDateTime을 직접 제공한다.
@Configuration
@EnableJpaAuditing(dateTimeProviderRef = "auditingDateTimeProvider")
public class JpaConfig {

    @Bean
    public DateTimeProvider auditingDateTimeProvider() {
        return () -> Optional.of(OffsetDateTime.now());
    }
}
