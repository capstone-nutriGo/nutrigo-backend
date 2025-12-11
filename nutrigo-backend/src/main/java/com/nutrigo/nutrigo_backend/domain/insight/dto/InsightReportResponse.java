package com.nutrigo.nutrigo_backend.domain.insight.dto;

import java.time.LocalDate;

public record InsightReportResponse(
        boolean success,
        Data data
) {
    public record Data(
            ReportRange range,
            LocalDate startDate,
            LocalDate endDate,
            Summary summary,
            Patterns patterns
    ) {
    }

    public record Summary(
            int totalMeals,
            int goodDays,
            int overeatDays,
            int lowSodiumDays,
            double avgScore
    ) {
    }

    public record Patterns(LateSnack lateSnack) {
    }

    public record LateSnack(int lateSnackDays) {
    }

    public enum ReportRange {
        WEEKLY,
        MONTHLY
    }
}