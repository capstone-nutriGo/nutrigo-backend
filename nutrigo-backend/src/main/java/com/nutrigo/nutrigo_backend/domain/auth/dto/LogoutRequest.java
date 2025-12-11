package com.nutrigo.nutrigo_backend.domain.auth.dto;

public record LogoutRequest(
        String refreshToken
) {
    public record Response(
            boolean success,
            Data data
    ) {
    }

    public record Data(boolean revoked) {
    }
}