package com.nutrigo.nutrigo_backend.domain.nutrition.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.AssertTrue;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartImageAnalysisRequest {

    @JsonProperty("image_url")
    private String imageUrl;

    @JsonProperty("image_base64")
    private String imageBase64;

    @JsonProperty("s3_key")
    private String s3Key; // S3 객체 키 (presigned URL 방식 사용 시)

    @JsonProperty("capture_id")
    private String captureId;

    @Valid
    @NotNull
    @JsonProperty("user_info")
    private UserInfoRequest userInfo;

    @AssertTrue(message = "imageUrl, imageBase64, s3Key 중 하나는 반드시 포함되어야 합니다.")
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    public boolean hasImageSource() {
        return (imageUrl != null && !imageUrl.isBlank())
                || (imageBase64 != null && !imageBase64.isBlank())
                || (s3Key != null && !s3Key.isBlank());
    }
}
