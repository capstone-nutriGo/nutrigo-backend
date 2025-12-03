package com.nutrigo.nutrigo_backend.domain.challenge.dto;

import java.time.LocalDateTime;

public record JoinChallengeResponse(
        boolean success,
        Data data
) {
    public record Data(
            Long challengeId,
            String status,
            LocalDateTime startedAt,
            LocalDateTime endedAt
    ) {
    }
}
