package com.nutrigo.nutrigo_backend.domain.challenge.dto;

import java.time.LocalDateTime;
import java.util.List;

public record ChallengeProgressResponse(
        boolean success,
        Data data
) {
    public record Data(
            List<InProgress> inProgress,
            List<Completed> done
    ) {
    }

    public record InProgress(
            Long challengeId,
            String title,
            String type,
            Integer progressRate,
            Integer remainingDays,
            List<DailyIntake> dailyIntakes
    ) {
    }

    public record DailyIntake(
            java.time.LocalDate date,
            Float totalKcal,
            Float totalSodiumMg,
            Float totalProteinG,
            Float totalCarbG,
            Integer totalSnack,
            Integer totalNight,
            Float dayScore,
            String dayColor
    ) {
    }

    public record Completed(
            Long challengeId,
            String title,
            String type,
            LocalDateTime finishedAt
    ) {
    }
}