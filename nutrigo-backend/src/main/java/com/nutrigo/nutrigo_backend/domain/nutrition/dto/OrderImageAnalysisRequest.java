package com.nutrigo.nutrigo_backend.domain.nutrition.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.nutrigo.nutrigo_backend.global.common.enums.MealTime;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderImageAnalysisRequest {

    @JsonProperty("image_url")
    private String imageUrl;

    @JsonProperty("image_base64")
    private String imageBase64;

    @JsonProperty("s3_key")
    private String s3Key; // S3 객체 키 (presigned URL 방식 사용 시)

    @JsonProperty("capture_id")
    private String captureId;

    // 클라이언트가 보내는 주문 날짜 / 시간대
    @NotNull
    @JsonProperty("order_date")
    private LocalDate orderDate;

    @NotNull
    @JsonProperty("meal_time")
    private MealTime mealTime;  // 아침/점심/저녁/간식 등 기존 enum 재사용

    @AssertTrue(message = "image_url, image_base64, s3_key 중 하나는 반드시 포함되어야 합니다.")
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    public boolean hasImageSource() {
        return (imageUrl != null && !imageUrl.isBlank())
                || (imageBase64 != null && !imageBase64.isBlank())
                || (s3Key != null && !s3Key.isBlank());
    }
}
