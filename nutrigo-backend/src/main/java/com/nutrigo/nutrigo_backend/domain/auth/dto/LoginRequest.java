package com.nutrigo.nutrigo_backend.domain.auth.dto;

public record LoginRequest(
        String email,
        String password
) {
}
