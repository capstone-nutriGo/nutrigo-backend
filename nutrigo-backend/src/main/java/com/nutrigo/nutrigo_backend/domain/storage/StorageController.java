package com.nutrigo.nutrigo_backend.domain.storage;

import com.nutrigo.nutrigo_backend.global.infra.s3.PresignedUrlResponse;
import com.nutrigo.nutrigo_backend.global.infra.s3.S3Service;
import com.nutrigo.nutrigo_backend.global.response.ApiResponse;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/storage")
@RequiredArgsConstructor
public class StorageController {

    private final S3Service s3Service;

    /**
     * POST /api/v1/storage/presigned-url
     * 이미지 업로드를 위한 presigned URL 생성
     *
     * @param request 파일 확장자와 MIME 타입
     * @return presigned URL과 S3 키 정보
     */
    @PostMapping("/presigned-url")
    public ResponseEntity<ApiResponse<PresignedUrlResponse>> generatePresignedUrl(
            @RequestBody PresignedUrlRequest request
    ) {
        PresignedUrlResponse response = s3Service.generatePresignedPutUrl(
                request.getContentType(),
                request.getFileExtension()
        );
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * POST /api/v1/storage/presigned-get-url
     * S3 객체 읽기를 위한 presigned GET URL 생성
     *
     * @param request S3 키
     * @return presigned GET URL
     */
    @PostMapping("/presigned-get-url")
    public ResponseEntity<ApiResponse<PresignedGetUrlResponse>> generatePresignedGetUrl(
            @RequestBody PresignedGetUrlRequest request
    ) {
        String presignedUrl = s3Service.generatePresignedGetUrl(request.getKey(), 3600);
        PresignedGetUrlResponse response = new PresignedGetUrlResponse(presignedUrl);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Data
    public static class PresignedUrlRequest {
        @NotBlank(message = "파일 확장자는 필수입니다.")
        private String fileExtension;

        @NotBlank(message = "Content-Type은 필수입니다.")
        private String contentType;
    }

    @Data
    public static class PresignedGetUrlRequest {
        @NotBlank(message = "S3 키는 필수입니다.")
        private String key;
    }

    @lombok.Data
    @lombok.AllArgsConstructor
    public static class PresignedGetUrlResponse {
        private String presignedUrl;
    }
}

