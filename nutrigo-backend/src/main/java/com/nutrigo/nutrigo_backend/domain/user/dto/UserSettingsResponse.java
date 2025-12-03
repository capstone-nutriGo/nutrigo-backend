package com.nutrigo.nutrigo_backend.domain.user.dto;

public record UserSettingsResponse(
        boolean success,
        Data data
) {
    public static UserSettingsResponse from(Boolean eveningCoach, Boolean challengeReminder, String defaultMode) {
        return new UserSettingsResponse(true, new Data(new Notification(eveningCoach, challengeReminder), defaultMode));
    }

    public record Data(
            Notification notification,
            String defaultMode
    ) {
    }

    public record Notification(
            Boolean eveningCoach,
            Boolean challengeReminder
    ) {
    }
}
