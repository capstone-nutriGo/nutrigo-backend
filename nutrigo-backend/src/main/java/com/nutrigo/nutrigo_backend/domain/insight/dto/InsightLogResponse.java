package com.nutrigo.nutrigo_backend.domain.insight.dto;

import java.util.List;

public record InsightLogResponse(
        boolean success,
        Data data
) {
    public record Data(Long mealLogId, List<Long> linkedChallengesUpdated) {
    }
}
