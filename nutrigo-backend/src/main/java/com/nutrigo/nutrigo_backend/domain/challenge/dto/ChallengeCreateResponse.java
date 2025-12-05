package com.nutrigo.nutrigo_backend.domain.challenge.dto;

import java.time.LocalDateTime;

public record ChallengeCreateResponse(
        boolean success,
        Data data
) {
    public record Data(
            Long challengeId,
            String title,
            String description,
            String category,
            String type,
            Integer durationDays,
            String status,
            LocalDateTime startedAt,
            LocalDateTime expectedEndAt,
            Goal goal
    ) {
    }

    public record Goal(
            Integer targetCount,
            Integer maxKcalPerMeal,
            Integer maxSodiumMgPerMeal,
            String customDescription
    ) {
    }
}