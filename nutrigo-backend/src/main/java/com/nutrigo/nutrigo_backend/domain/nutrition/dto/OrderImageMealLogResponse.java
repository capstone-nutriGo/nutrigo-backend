package com.nutrigo.nutrigo_backend.domain.nutrition.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.nutrigo.nutrigo_backend.global.common.enums.MealTime;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderImageMealLogResponse {

    @JsonProperty("capture_id")
    private String captureId;

    private List<MealLogCandidateResponse> items;

    @JsonProperty("raw_ocr_text")
    private String rawOcrText;   // 옵션: 전체 OCR 텍스트

    // 요청에서 받았던 주문 날짜
    @JsonProperty("order_date")
    private LocalDate orderDate;

    // 요청에서 받았던 식사 시간대 (BREAKFAST / LUNCH / DINNER / SNACK)
    @JsonProperty("meal_time")
    private MealTime mealTime;
}
