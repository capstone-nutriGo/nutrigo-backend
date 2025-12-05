package com.nutrigo.nutrigo_backend.domain.auth.dto;

import com.nutrigo.nutrigo_backend.domain.user.User;
import com.nutrigo.nutrigo_backend.domain.user.UserPreferences;

public record AuthResponse(
        boolean success,
        TokenData data
) {
    public static AuthResponse from(String accessToken, String refreshToken, User user, UserPreferences preferences) {
        return new AuthResponse(true, new TokenData(
                accessToken,
                refreshToken,
                "Bearer",
                3600,
                new UserData(
                        user.getId(),
                        user.getEmail(),
                        user.getNickname()
                )
        ));
    }

    public record TokenData(
            String accessToken,
            String refreshToken,
            String tokenType,
            int expiresIn,
            UserData user
    ) {
    }

    public record UserData(
            Long userId,
            String email,
            String nickname
    ) {
    }
}