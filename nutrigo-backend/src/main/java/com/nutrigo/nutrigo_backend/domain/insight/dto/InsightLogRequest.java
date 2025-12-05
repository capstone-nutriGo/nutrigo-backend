package com.nutrigo.nutrigo_backend.domain.insight.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.nutrigo.nutrigo_backend.global.common.enums.MealTime;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.OffsetDateTime;

public record InsightLogRequest(
        @NotBlank String source,

        @Schema(description = "Analysis Session ID", example = "1")
        @NotNull Long analysisId,
        @NotNull MealTime mealtime,
        @NotNull @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSX") OffsetDateTime orderedAt
) {
}