package com.nutrigo.nutrigo_backend.domain.challenge.dto;

public record ChallengeCreateRequest(
        String title,
        String description,
        String category,
        String type,
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