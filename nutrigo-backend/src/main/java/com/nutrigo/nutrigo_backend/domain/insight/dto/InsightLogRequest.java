package com.nutrigo.nutrigo_backend.domain.insight.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.nutrigo.nutrigo_backend.global.common.enums.MealTime;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record InsightLogRequest(
        @Schema(description = "식사한 메뉴명", example = "치킨 샐러드")
        String menu,

        @Schema(description = "분석에 사용할 음식 이미지 주소", example = "https://example.com/images/meal.jpg")
        String foodImageUrl,

        @Schema(description = "분석에 사용할 음식 텍스트 정보", example = "현미밥 한 공기와 닭가슴살 150g")
        String foodDescription,

        @Schema(description = "식사량(인분 단위)", example = "0.8")
        @NotNull(message = "식사량을 입력해주세요")
        Float serving,

        @NotNull(message = "식사 종류를 선택해주세요")
        MealTime mealtime,

        @NotNull(message = "식사 날짜를 선택해주세요")
        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate mealDate,

        // 이미 분석된 영양소 정보 (선택적, 있으면 우선 사용)
        @Schema(description = "칼로리 (kcal)", example = "500.0")
        Float kcal,

        @Schema(description = "나트륨 (mg)", example = "800.0")
        Float sodiumMg,

        @Schema(description = "단백질 (g)", example = "30.0")
        Float proteinG,

        @Schema(description = "탄수화물 (g)", example = "60.0")
        Float carbG,

        @Schema(description = "카테고리", example = "한식")
        String category
) {
}