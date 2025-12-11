package com.nutrigo.nutrigo_backend.domain.insight.dto;

import java.time.LocalDate;
import java.util.List;

public record WeeklyInsightSummaryResponse(
        boolean success,
        Data data
) {
    public record Data(
            LocalDate weekStart,
            LocalDate weekEnd,
            Summary summary,
            Trends trends,
            List<CategoryStat> categoryTop3
    ) {
    }

    public record Summary(
            Integer totalMeals,
            Integer goodDays,
            Integer overeatDays,
            Integer lowSodiumDays,
            Double averageScore,
            Double averageKcalPerMeal
    ) {
    }

    public record Trends(
            List<TrendDay> days
    ) {
    }

    public record TrendDay(
            LocalDate date,
            Float dayScore,
            String dayColor,
            Float totalKcal,
            Float totalCarbG,
            Float totalProteinG,
            Float totalSodiumMg
    ) {
    }

    public record CategoryStat(
            String category,
            Long count
    ) {
    }
}