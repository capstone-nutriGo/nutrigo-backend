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
        Float serving,

        @Schema(description = "식사 칼로리", example = "520")
        Float kcal,

        @Schema(description = "식사 나트륨(mg)", example = "800")
        Float sodiumMg,

        @Schema(description = "식사 단백질(g)", example = "35")
        Float proteinG,

        @Schema(description = "식사 탄수화물(g)", example = "40")
        Float carbG,

        @Schema(description = "식사 종합 점수", example = "82.5")
        Float totalScore,

        @NotNull(message = "식사 종류를 선택해주세요")
        MealTime mealtime,

        @NotNull(message = "식사 날짜를 선택해주세요")
        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate mealDate
) {
}