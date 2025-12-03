package com.nutrigo.nutrigo_backend.domain.insight.dto;

import com.nutrigo.nutrigo_backend.global.common.enums.MealTime;

import java.time.LocalDate;
import java.time.OffsetDateTime;
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
            Integer totalMeals,
            List<Meal> meals
    ) {
    }

    public record Meal(
            Long mealLogId,
            String source,
            MealTime mealTime,
            OffsetDateTime orderedAt
    ) {
    }
}
