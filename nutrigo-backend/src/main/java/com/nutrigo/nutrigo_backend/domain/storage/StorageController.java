package com.nutrigo.nutrigo_backend.domain.storage;

import com.nutrigo.nutrigo_backend.domain.nutrition.ImageUploadService;
import com.nutrigo.nutrigo_backend.domain.nutrition.dto.ImageUploadUrlResponse;
import com.nutrigo.nutrigo_backend.domain.nutrition.dto.UploadType;
import com.nutrigo.nutrigo_backend.global.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/storage")
@RequiredArgsConstructor
public class StorageController {

    private final ImageUploadService imageUploadService;
    private final S3Presigner s3Presigner;

    @Value("${nutrigo.s3.bucket-name:nutrigo-images}")
    private String bucket;

    @Value("${nutrigo.s3.presigned-url-expiration:3600}")
    private long expirationSeconds;

    /**
     * POST /api/v1/storage/presigned-url
     * 이미지 업로드용 presigned PUT URL 발급
     * 프론트엔드에서 fileExtension과 contentType을 받아서 처리
     */
    @PostMapping("/presigned-url")
    public ResponseEntity<ApiResponse<Map<String, Object>>> createPresignedUrl(
            @RequestBody @Valid PresignedUrlRequest request
    ) {
        // fileExtension에서 contentType 추론 또는 요청에서 받은 contentType 사용
        String contentType = request.getContentType() != null && !request.getContentType().isBlank()
                ? request.getContentType()
                : guessContentType(request.getFileExtension());

        // ORDER_IMAGE 타입으로 presigned URL 생성 (식사 기록용)
        ImageUploadUrlResponse response = imageUploadService.createUploadUrl(
                UploadType.ORDER_IMAGE,
                contentType
        );

        // 프론트엔드가 기대하는 형식으로 변환
        Map<String, Object> result = new HashMap<>();
        result.put("presignedUrl", response.getUploadUrl());
        result.put("key", response.getKey());
        long expiresIn = response.getExpiresAt().getEpochSecond() - java.time.Instant.now().getEpochSecond();
        result.put("expiresIn", expiresIn);
        result.put("contentType", response.getContentType());

        return ResponseEntity.ok(ApiResponse.success(result));
    }

    /**
     * POST /api/v1/storage/presigned-get-url
     * 이미지 조회용 presigned GET URL 발급
     */
    @PostMapping("/presigned-get-url")
    public ResponseEntity<ApiResponse<Map<String, String>>> createPresignedGetUrl(
            @RequestBody @Valid PresignedGetUrlRequest request
    ) {
        if (request.getKey() == null || request.getKey().isBlank()) {
            throw new IllegalArgumentException("key는 필수입니다.");
        }

        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucket)
                .key(request.getKey())
                .build();

        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofSeconds(expirationSeconds))
                .getObjectRequest(getObjectRequest)
                .build();

        PresignedGetObjectRequest presignedRequest = s3Presigner.presignGetObject(presignRequest);
        String presignedUrl = presignedRequest.url().toString();

        Map<String, String> result = new HashMap<>();
        result.put("presignedUrl", presignedUrl);

        return ResponseEntity.ok(ApiResponse.success(result));
    }

    private String guessContentType(String fileExtension) {
        if (fileExtension == null || fileExtension.isBlank()) {
            return "image/jpeg";
        }
        String ext = fileExtension.toLowerCase();
        if (ext.equals("png")) return "image/png";
        if (ext.equals("jpg") || ext.equals("jpeg")) return "image/jpeg";
        if (ext.equals("webp")) return "image/webp";
        return "image/jpeg";
    }

    // 내부 DTO 클래스들
    public static class PresignedUrlRequest {
        private String fileExtension;
        private String contentType;

        public String getFileExtension() {
            return fileExtension;
        }

        public void setFileExtension(String fileExtension) {
            this.fileExtension = fileExtension;
        }

        public String getContentType() {
            return contentType;
        }

        public void setContentType(String contentType) {
            this.contentType = contentType;
        }
    }

    public static class PresignedGetUrlRequest {
        private String key;

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }
    }
}

