package com.nutrigo.nutrigo_backend.domain.insight.dto;

import com.nutrigo.nutrigo_backend.global.common.enums.MealTime;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record DayMealsResponse(
        boolean success,
        Data data
) {
    public record Data(
            LocalDate date,
            Float totalKcal,
            Float totalSodiumMg,
            Float totalProteinG,
            Float totalCarbG,
            Integer totalMeals,
            Integer totalSnack,
            Integer totalNight,
            Float dayScore,
            String dayColor,
            List<Meal> meals
    ) {
    }

    public record Meal(
            Long mealLogId,
            String menu,
            String category,
            MealTime mealTime,
            LocalDate mealDate,
            LocalDateTime createdAt,
            Float kcal,
            Float sodiumMg,
            Float proteinG,
            Float carbG,
            Float totalScore
    ) {
    }
}