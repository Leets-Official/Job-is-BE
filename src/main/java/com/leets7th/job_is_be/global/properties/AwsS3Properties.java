package com.leets7th.job_is_be.global.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "app.aws.s3")
public record AwsS3Properties(
        String bucket,
        String region,
        Duration presignedUrlExpiration
) {
}
