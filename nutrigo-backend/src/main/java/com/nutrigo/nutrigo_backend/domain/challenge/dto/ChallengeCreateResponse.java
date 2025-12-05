package com.nutrigo.nutrigo_backend.domain.challenge.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record ChallengeCreateResponse(
        boolean success,
        Data data
) {
    public record Data(
            Long challengeId,
            String title,
            String description,
            String type,
            Integer durationDays,
            String status,
            LocalDate startedAt,
            LocalDate expectedEndAt
    ) {
    }
}