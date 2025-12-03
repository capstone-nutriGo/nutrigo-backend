package com.nutrigo.nutrigo_backend.domain.user.dto;

import com.nutrigo.nutrigo_backend.domain.user.User;
import com.nutrigo.nutrigo_backend.domain.user.UserPreferences;

import java.time.LocalDate;

public record UserProfileResponse(
        boolean success,
        Data data
) {
    public static UserProfileResponse from(User user) {
        UserPreferences prefs = user.getPreferences();
        return new UserProfileResponse(true, new Data(
                user.getId(),
                user.getEmail(),
                user.getNickname(),
                user.getName(),
                user.getGender() != null ? user.getGender().name() : null,
                user.getBirthday(),
                user.getAddress(),
                prefs != null ? new Preferences(
                        prefs.getHealthMode() != null ? prefs.getHealthMode().name() : null,
                        prefs.getDefaultMode() != null ? prefs.getDefaultMode().name() : null
                ) : null
        ));
    }

    public record Data(
            Long userId,
            String email,
            String nickname,
            String name,
            String gender,
            LocalDate birthday,
            String address,
            Preferences preferences
    ) {
    }

    public record Preferences(
            String healthMode,
            String defaultMode
    ) {
    }
}
