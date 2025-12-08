package com.nutrigo.nutrigo_backend.domain.user.dto;

import com.nutrigo.nutrigo_backend.domain.user.User;
import com.nutrigo.nutrigo_backend.domain.user.UserSetting;

import java.time.LocalDate;

public record UserProfileResponse(
        boolean success,
        Data data
) {
    public static UserProfileResponse from(User user) {
        UserSetting prefs = user.getPreferences();
        return new UserProfileResponse(true, new Data(
                user.getId(),
                user.getEmail(),
                user.getNickname(),
                user.getName(),
                user.getGender() != null ? user.getGender().name() : null,
                user.getBirthday(),
                prefs != null ? prefs.getEveningCoach() : null,
                prefs != null ? prefs.getChallengeReminder() : null
        ));
    }

    public record Data(
            Long userId,
            String email,
            String nickname,
            String name,
            String gender,
            LocalDate birthday,
            Boolean eveningCoach,
            Boolean challengeReminder
    ) {
    }
}