package com.nutrigo.nutrigo_backend.global.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
public class S3Config {

    @Value("${nutrigo.s3.region:ap-northeast-2}")
    private String region;

    @Value("${nutrigo.s3.access-key:}")
    private String accessKey;

    @Value("${nutrigo.s3.secret-key:}")
    private String secretKey;

    @Bean
    public S3Client s3Client() {
        // 명시적으로 UrlConnectionHttpClient를 사용하여 HTTP 구현체 충돌 방지
        var builder = S3Client.builder()
                .region(Region.of(region))
                .httpClient(UrlConnectionHttpClient.create());

        // 자격 증명이 제공된 경우에만 설정
        if (accessKey != null && !accessKey.isBlank() && secretKey != null && !secretKey.isBlank()) {
            AwsBasicCredentials awsCredentials = AwsBasicCredentials.create(accessKey, secretKey);
            builder.credentialsProvider(StaticCredentialsProvider.create(awsCredentials));
        }

        return builder.build();
    }
}

