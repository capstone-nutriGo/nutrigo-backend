package com.nutrigo.nutrigo_backend.domain.user.dto;

public record UserSettingsResponse(
        boolean success,
        Data data
) {
    public static UserSettingsResponse from(Boolean eveningCoach, Boolean challengeReminder) {
        return new UserSettingsResponse(true, new Data(new Notification(eveningCoach, challengeReminder)));
    }

    public record Data(
            Notification notification
    ) {
    }

    public record Notification(
            Boolean eveningCoach,
            Boolean challengeReminder
    ) {
    }
}