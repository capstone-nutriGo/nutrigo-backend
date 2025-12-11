package com.nutrigo.nutrigo_backend.global.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.S3ClientBuilder;

import java.net.URI;
import java.util.Optional;

@Configuration
public class S3Config {

    @Value("${nutrigo.s3.region:ap-northeast-2}")
    private String region;

    @Value("${nutrigo.s3.endpoint:}")
    private String endpoint;

    @Value("${nutrigo.s3.access-key:}")
    private String accessKey;

    @Value("${nutrigo.s3.secret-key:}")
    private String secretKey;

    private Region resolveRegion() {
        if (region != null && !region.isBlank()) {
            return Region.of(region);
        }
        return Region.US_EAST_1; // 기본값 (환경변수/설정으로 덮어쓰기 권장)
    }

    private AwsCredentialsProvider resolveCredentialsProvider() {
        if (accessKey != null && !accessKey.isBlank()
                && secretKey != null && !secretKey.isBlank()) {
            return StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey));
        }
        return DefaultCredentialsProvider.create();
    }

    private S3ClientBuilder applyEndpoint(S3ClientBuilder builder) {
        if (endpoint != null && !endpoint.isBlank()) {
            builder.endpointOverride(URI.create(endpoint));
        }
        return builder;
    }

    private S3Presigner.Builder applyEndpoint(S3Presigner.Builder builder) {
        if (endpoint != null && !endpoint.isBlank()) {
            builder.endpointOverride(URI.create(endpoint));
        }
        return builder;
    }

    @Bean
    public S3Client s3Client() {
        return applyEndpoint(
                S3Client.builder()
                        .region(resolveRegion())
                        .credentialsProvider(resolveCredentialsProvider())
        ).build();
    }

    @Bean
    public S3Presigner s3Presigner() {
        return applyEndpoint(
                S3Presigner.builder()
                        .region(resolveRegion())
                        .credentialsProvider(resolveCredentialsProvider())
        ).build();
    }
}

