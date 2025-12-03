package com.nutrigo.nutrigo_backend.domain.auth.dto;

public record SocialLoginRequest(
        Provider provider,
        String accessToken,
        String idToken,
        DeviceInfo deviceInfo
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