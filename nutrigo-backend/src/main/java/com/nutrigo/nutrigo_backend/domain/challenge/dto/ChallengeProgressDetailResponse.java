package com.nutrigo.nutrigo_backend.domain.challenge.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record ChallengeProgressDetailResponse(
        boolean success,
        Data data
) {
    public record Data(
            Long challengeId,
            String title,
            String description,
            String type,
            String status,
            Integer progressRate,
            Integer remainingDays,
            Integer totalDays,
            Integer completedDays,
            LocalDate startedAt,
            LocalDate endedAt,
            LocalDateTime finishedAt,
            List<ChallengeProgressResponse.DailyIntake> dailyIntakes
    ) {
    }
}