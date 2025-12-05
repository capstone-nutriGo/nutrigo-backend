package com.nutrigo.nutrigo_backend.domain.challenge.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record JoinChallengeResponse(
        boolean success,
        Data data
) {
    public record Data(
            Long challengeId,
            String status,
            LocalDate startedAt,
            LocalDate endedAt
    ) {
    }
}