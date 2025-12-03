package com.nutrigo.nutrigo_backend.domain.auth.dto;

import com.nutrigo.nutrigo_backend.global.common.enums.Gender;

public record RegisterRequest(
        String email,
        String password,
        String nickname,
        String name,
        Gender gender,
        String birthday
) {
}
