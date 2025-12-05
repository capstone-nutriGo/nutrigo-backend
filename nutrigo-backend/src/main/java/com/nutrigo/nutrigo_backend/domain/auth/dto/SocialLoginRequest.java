package com.nutrigo.nutrigo_backend.domain.auth.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public record SocialLoginRequest(
        @NotNull(message = "소셜 로그인 제공자를 선택해주세요")
        Provider provider,
        String accessToken,
        String idToken,
        @Valid DeviceInfo deviceInfo
) {
    public enum Provider {
        KAKAO,
        GOOGLE,
        NAVER
    }

    public record DeviceInfo(
            String os,
            String appVersion
    ) {
    }
}