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

    @JsonProperty("capture_id")
    private String captureId;

    @Valid
    @NotNull
    @JsonProperty("user_goal")
    private UserGoalRequest userGoal;

    @AssertTrue(message = "imageUrl 또는 imageBase64 중 하나는 반드시 포함되어야 합니다.")
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    public boolean hasImageSource() {
        return (imageUrl != null && !imageUrl.isBlank())
                || (imageBase64 != null && !imageBase64.isBlank());
    }
}
