package com.nutrigo.nutrigo_backend.domain.insight.dto;

import com.nutrigo.nutrigo_backend.domain.insight.DayHighlight;

import java.time.LocalDate;
import java.util.List;

public record InsightCalendarResponse(
        boolean success,
        Data data
) {
    public record Data(
            LocalDate startDate,
            LocalDate endDate,
            List<Day> days
    ) {
    }

    public record Day(
            LocalDate date,
            List<String> tags,
            Float dayScore,
            DayHighlight highlight
    ) {
    }
}