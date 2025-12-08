package com.nutrigo.nutrigo_backend.domain.challenge.dto;

import java.time.LocalDateTime;

public record ChallengeQuitResponse(
        boolean success,
        Data data
) {
    public record Data(
            Long challengeId,
            String status,
            LocalDateTime finishedAt
    ) {
    }
}