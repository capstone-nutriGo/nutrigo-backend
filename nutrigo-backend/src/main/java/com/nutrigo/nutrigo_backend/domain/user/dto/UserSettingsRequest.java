package com.nutrigo.nutrigo_backend.domain.user.dto;

import jakarta.validation.Valid;

public record UserSettingsRequest(
        @Valid Notification notification
) {
    public record Notification(
            Boolean eveningCoach,
            Boolean challengeReminder
    ) {
    }
}