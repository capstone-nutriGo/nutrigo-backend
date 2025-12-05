package com.nutrigo.nutrigo_backend.domain.user.dto;

import com.nutrigo.nutrigo_backend.global.common.enums.DefaultMode;
import com.nutrigo.nutrigo_backend.global.common.enums.Gender;
import com.nutrigo.nutrigo_backend.global.common.enums.HealthMode;

import java.time.LocalDate;

public record UserProfileUpdateRequest(
        String nickname,
        String name,
        Gender gender,
        LocalDate birthday,
        String address,
        Preferences preferences
) {
    public record Preferences(
            HealthMode healthMode,
            DefaultMode defaultMode
    ) {
    }
}