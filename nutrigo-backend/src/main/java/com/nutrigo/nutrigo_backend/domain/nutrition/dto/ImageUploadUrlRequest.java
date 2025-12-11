package com.nutrigo.nutrigo_backend.domain.nutrition.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ImageUploadUrlRequest {

    @NotNull
    @JsonProperty("type")
    private UploadType type;

    @NotBlank
    @JsonProperty("content_type")
    private String contentType;
}

