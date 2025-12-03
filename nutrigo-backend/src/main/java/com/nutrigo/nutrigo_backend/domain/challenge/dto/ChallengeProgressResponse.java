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
            String category,
            String type,
            Integer progressRate,
            Integer logsCount,
            Integer remainingDays
    ) {
    }

    public record Completed(
            Long challengeId,
            String title,
            String category,
            String type,
            LocalDateTime finishedAt
    ) {
    }
}
