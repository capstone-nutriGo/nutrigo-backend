package com.nutrigo.nutrigo_backend.domain.auth.dto;

import com.nutrigo.nutrigo_backend.domain.user.User;
import com.nutrigo.nutrigo_backend.domain.user.UserSetting;

public record AuthResponse(
        boolean success,
        TokenData data
) {
    public static AuthResponse from(String accessToken, String refreshToken, User user, UserSetting preferences) {
        boolean profileComplete = user.getGender() != null
                && user.getBirthday() != null
                && !user.getBirthday().equals(User.SOCIAL_PLACEHOLDER_BIRTHDAY);
        return new AuthResponse(true, new TokenData(
                accessToken,
                refreshToken,
                "Bearer",
                3600,
                new UserData(
                        user.getId(),
                        user.getEmail(),
                        user.getNickname()
                ),
                null,
                profileComplete
        ));
    }

    public AuthResponse appendProviderAccessToken(String providerAccessToken) {
        TokenData tokenData = this.data();
        return new AuthResponse(this.success(), new TokenData(
                tokenData.accessToken(),
                tokenData.refreshToken(),
                tokenData.tokenType(),
                tokenData.expiresIn(),
                tokenData.user(),
                providerAccessToken,
                tokenData.profileComplete()
        ));
    }

    public record TokenData(
            String accessToken,
            String refreshToken,
            String tokenType,
            int expiresIn,
            UserData user,
            String providerAccessToken,
            boolean profileComplete
    ) {
    }

    public record UserData(
            Long userId,
            String email,
            String nickname
    ) {
    }
}