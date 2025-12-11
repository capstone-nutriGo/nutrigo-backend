package com.nutrigo.nutrigo_backend.domain.nutrition.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ImageUploadUrlResponse {

    @JsonProperty("key")
    private String key;

    @JsonProperty("upload_url")
    private String uploadUrl;

    @JsonProperty("expires_at")
    private Instant expiresAt;

    @JsonProperty("content_type")
    private String contentType;
}

