package com.nutrigo.nutrigo_backend.domain.nutrition;

import com.nutrigo.nutrigo_backend.domain.nutrition.dto.ImageUploadUrlRequest;
import com.nutrigo.nutrigo_backend.domain.nutrition.dto.ImageUploadUrlResponse;
import com.nutrigo.nutrigo_backend.domain.nutrition.dto.UploadType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.net.URL;
import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ImageUploadService {

    private final S3Presigner presigner;

    @Value("${aws.s3.bucket}")
    private String bucket;

    @Value("${aws.s3.url-expiration-seconds:900}")
    private long expirationSeconds;

    /**
     * 업로드용 presigned URL 생성
     */
    public ImageUploadUrlResponse createUploadUrl(ImageUploadUrlRequest request) {
        return createUploadUrl(request.getType(), request.getContentType());
    }

    public ImageUploadUrlResponse createUploadUrl(UploadType type, String requestedContentType) {
        UploadType resolvedType = (type != null) ? type : UploadType.CART_IMAGE;
        String contentType = (requestedContentType != null && !requestedContentType.isBlank())
                ? requestedContentType
                : "image/jpeg";

        String key = buildObjectKey(resolvedType, contentType);
        PresignedPutObjectRequest presigned = presignPutObject(key, contentType);

        Instant expiresAt = Instant.now().plusSeconds(expirationSeconds);
        log.info("[ImageUploadService] created presigned upload: type={}, key={}, expiresAt={}", resolvedType, key, expiresAt);

        return ImageUploadUrlResponse.builder()
                .key(key)
                .uploadUrl(presigned.url().toString())
                .expiresAt(expiresAt)
                .contentType(contentType)
                .build();
    }

    private String buildObjectKey(UploadType type, String contentType) {
        String extension = guessExtension(contentType);
        String uuid = UUID.randomUUID().toString();
        return String.format("nutrition/%s/%s.%s",
                type.name().toLowerCase(Locale.ROOT),
                uuid,
                extension);
    }

    private PresignedPutObjectRequest presignPutObject(String key, String contentType) {
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType(contentType)
                .build();

        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(Duration.ofSeconds(expirationSeconds))
                .putObjectRequest(putObjectRequest)
                .build();

        return presigner.presignPutObject(presignRequest);
    }

    private String guessExtension(String contentType) {
        if (contentType == null) {
            return "jpg";
        }
        String ct = contentType.toLowerCase(Locale.ROOT);
        if (ct.contains("png")) return "png";
        if (ct.contains("jpeg") || ct.contains("jpg")) return "jpg";
        if (ct.contains("webp")) return "webp";
        return "jpg";
    }
}

