package com.nutrigo.nutrigo_backend.domain.insight.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.nutrigo.nutrigo_backend.global.common.enums.MealTime;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

public record MealAnalysisRequest(
        @Schema(description = "사용자가 입력한 메뉴명", example = "치킨 샐러드")
        String menu,
        @Schema(description = "S3에 저장된 음식 이미지 URL", example = "https://example.com/images/meal.jpg")
        String foodImageUrl,
        @Schema(description = "OCR과 함께 사용할 텍스트 설명", example = "현미밥 한 공기와 닭가슴살 150g")
        String foodDescription,
        @Schema(description = "식사량(인분)", example = "0.8")
        Float serving,
        @Schema(description = "식사 구분", example = "BREAKFAST")
        MealTime mealtime,
        @Schema(description = "식사 일자", example = "2025-12-08")
        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate mealDate
) {
}