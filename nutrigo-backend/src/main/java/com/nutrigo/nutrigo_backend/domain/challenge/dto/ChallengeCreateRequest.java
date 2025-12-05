package com.nutrigo.nutrigo_backend.domain.challenge.dto;

import com.nutrigo.nutrigo_backend.global.common.enums.ChallengeCategory;
import com.nutrigo.nutrigo_backend.global.common.enums.ChallengeType;

public record ChallengeCreateRequest(
        String title,
        String description,
        ChallengeCategory category,
        ChallengeType type,
        Integer durationDays,
        Goal goal
) {
    public record Goal(
            Integer targetCount,
            Integer maxKcalPerMeal,
            Integer maxSodiumMgPerMeal,
            String customDescription
    ) {
    }
}