package com.nutrigo.nutrigo_backend.domain.nutrition.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

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
}
