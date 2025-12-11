package com.nutrigo.nutrigo_backend.global.infra.s3;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PresignedUrlResponse {
    private String presignedUrl;
    private String key;
    private int expiresIn; // 초 단위
    private String contentType;
}

