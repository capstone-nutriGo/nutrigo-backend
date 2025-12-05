package com.nutrigo.nutrigo_backend.domain.auth;

import com.nutrigo.nutrigo_backend.domain.auth.dto.AuthResponse;
import com.nutrigo.nutrigo_backend.domain.auth.dto.LoginRequest;
import com.nutrigo.nutrigo_backend.domain.auth.dto.LogoutRequest;
import com.nutrigo.nutrigo_backend.domain.auth.dto.RefreshRequest;
import com.nutrigo.nutrigo_backend.domain.auth.dto.RegisterRequest;
import com.nutrigo.nutrigo_backend.domain.auth.dto.SocialLoginRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid@RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid@RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/logout")
    public ResponseEntity<LogoutRequest.Response> logout(@RequestHeader("Authorization") String authorization,
                                                         @Valid@RequestBody(required = false) LogoutRequest request) {
        return ResponseEntity.ok(authService.logout(authorization, request));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@Valid@RequestBody RefreshRequest request) {
        return ResponseEntity.ok(authService.refresh(request));
    }

    @PostMapping("/social/login")
    public ResponseEntity<AuthResponse> socialLogin(@Valid@RequestBody SocialLoginRequest request) {
        return ResponseEntity.ok(authService.socialLogin(request));
    }
}