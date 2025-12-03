package com.nutrigo.nutrigo_backend.domain.insight.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.nutrigo.nutrigo_backend.global.common.enums.MealTime;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.OffsetDateTime;

public record InsightLogRequest(
        @NotBlank String source,
        @NotNull Long analysisId,
        @NotNull MealTime mealtime,
        @NotNull @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssXXX") OffsetDateTime orderedAt
) {
}