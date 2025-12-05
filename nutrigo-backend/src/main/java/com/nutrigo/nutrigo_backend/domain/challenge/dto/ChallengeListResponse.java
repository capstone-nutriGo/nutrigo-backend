package com.nutrigo.nutrigo_backend.domain.challenge.dto;

import java.time.LocalDateTime;
import java.util.List;

public record ChallengeListResponse(
        boolean success,
        Data data
) {
    public record Data(List<ChallengeSummary> challenges) {
    }

    public record ChallengeSummary(
            Long challengeId,
            String title,
            String description,
            String category,
            String type,
            Integer durationDays,
            String status,
            Double progressValue,
            LocalDateTime startedAt,
            LocalDateTime endedAt
    ) {
    }
}