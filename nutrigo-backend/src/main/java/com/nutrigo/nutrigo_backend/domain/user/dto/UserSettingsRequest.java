package com.nutrigo.nutrigo_backend.domain.user.dto;

import com.nutrigo.nutrigo_backend.global.common.enums.DefaultMode;
import jakarta.validation.Valid;

public record UserSettingsRequest(
        @Valid Notification notification,
        DefaultMode defaultMode
) {
    public record Notification(
            Boolean eveningCoach,
            Boolean challengeReminder
    ) {
    }
}