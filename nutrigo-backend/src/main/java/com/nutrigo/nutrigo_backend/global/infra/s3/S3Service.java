package com.nutrigo.nutrigo_backend.global.infra.s3;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.time.Duration;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class S3Service {

    private final S3Client s3Client;

    @Value("${nutrigo.s3.access-key:}")
    private String accessKey;

    @Value("${nutrigo.s3.secret-key:}")
    private String secretKey;

    @Value("${nutrigo.s3.bucket-name}")
    private String bucketName;

    @Value("${nutrigo.s3.region}")
    private String region;

    @Value("${nutrigo.s3.presigned-url-expiration:3600}")
    private int presignedUrlExpiration;

    /**
     * 이미지 업로드를 위한 presigned PUT URL 생성
     *
     * @param contentType 이미지 MIME 타입 (예: "image/jpeg", "image/png")
     * @param fileExtension 파일 확장자 (예: "jpg", "png")
     * @return PresignedUrlResponse presigned URL과 S3 키 정보
     */
    public PresignedUrlResponse generatePresignedPutUrl(String contentType, String fileExtension) {
        // S3 키 생성: uploads/{userId}/{timestamp}-{uuid}.{extension}
        String key = String.format("uploads/%s/%s.%s",
                "temp", // TODO: 실제 사용자 ID로 변경
                UUID.randomUUID().toString(),
                fileExtension);

        S3Presigner presigner = createPresigner();
        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .contentType(contentType)
                    .build();

            PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                    .signatureDuration(Duration.ofSeconds(presignedUrlExpiration))
                    .putObjectRequest(putObjectRequest)
                    .build();

            PresignedPutObjectRequest presignedRequest = presigner.presignPutObject(presignRequest);
            String presignedUrl = presignedRequest.url().toString();

            log.info("Presigned PUT URL 생성 완료: key={}, expiresIn={}초", key, presignedUrlExpiration);

            return PresignedUrlResponse.builder()
                    .presignedUrl(presignedUrl)
                    .key(key)
                    .expiresIn(presignedUrlExpiration)
                    .contentType(contentType)
                    .build();
        } finally {
            presigner.close();
        }
    }

    /**
     * S3 객체에 대한 presigned GET URL 생성 (읽기용)
     *
     * @param key S3 객체 키
     * @param expirationSeconds 만료 시간 (초)
     * @return presigned GET URL
     */
    public String generatePresignedGetUrl(String key, int expirationSeconds) {
        S3Presigner presigner = createPresigner();
        try {
            software.amazon.awssdk.services.s3.model.GetObjectRequest getObjectRequest =
                    software.amazon.awssdk.services.s3.model.GetObjectRequest.builder()
                            .bucket(bucketName)
                            .key(key)
                            .build();

            software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest presignedGetRequest =
                    presigner.presignGetObject(builder -> builder
                            .signatureDuration(Duration.ofSeconds(expirationSeconds))
                            .getObjectRequest(getObjectRequest));

            return presignedGetRequest.url().toString();
        } finally {
            presigner.close();
        }
    }

    /**
     * S3Presigner 생성
     */
    private S3Presigner createPresigner() {
        var builder = S3Presigner.builder()
                .region(Region.of(region));

        // 자격 증명이 제공된 경우에만 설정
        if (accessKey != null && !accessKey.isBlank() && secretKey != null && !secretKey.isBlank()) {
            AwsBasicCredentials awsCredentials = AwsBasicCredentials.create(accessKey, secretKey);
            builder.credentialsProvider(StaticCredentialsProvider.create(awsCredentials));
        } else {
            // 자격 증명이 없으면 명확한 에러 메시지와 함께 예외 발생
            throw new IllegalStateException(
                    "AWS 자격 증명이 설정되지 않았습니다. " +
                    "환경 변수 AWS_ACCESS_KEY_ID와 AWS_SECRET_ACCESS_KEY를 설정하거나 " +
                    "application.yml의 nutrigo.s3.access-key와 nutrigo.s3.secret-key를 설정해주세요."
            );
        }

        return builder.build();
    }

    /**
     * S3 객체의 공개 URL 생성 (버킷이 public인 경우)
     */
    public String getPublicUrl(String key) {
        return String.format("https://%s.s3.%s.amazonaws.com/%s", bucketName, region, key);
    }
}

