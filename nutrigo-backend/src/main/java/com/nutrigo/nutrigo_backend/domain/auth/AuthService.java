package com.nutrigo.nutrigo_backend.domain.auth;

import com.nutrigo.nutrigo_backend.domain.auth.dto.AuthResponse;
import com.nutrigo.nutrigo_backend.domain.auth.dto.LoginRequest;
import com.nutrigo.nutrigo_backend.domain.auth.dto.LogoutRequest;
import com.nutrigo.nutrigo_backend.domain.auth.dto.RefreshRequest;
import com.nutrigo.nutrigo_backend.domain.auth.dto.RegisterRequest;
import com.nutrigo.nutrigo_backend.domain.auth.dto.SocialLoginRequest;
import com.nutrigo.nutrigo_backend.domain.user.User;
import com.nutrigo.nutrigo_backend.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseGet(() -> createUserFromRegister(request));
        String accessToken = generateToken();
        String refreshToken = generateToken();
        return AuthResponse.from(accessToken, refreshToken, user, null);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .filter(existing -> existing.getPassword() != null && existing.getPassword().equals(request.password()))
                .orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));
        String accessToken = generateToken();
        String refreshToken = generateToken();
        return AuthResponse.from(accessToken, refreshToken, user, user.getPreferences());
    }

    public LogoutRequest.Response logout(String authorization, LogoutRequest request) {
        boolean revoked = authorization != null && authorization.toLowerCase().startsWith("bearer");
        return new LogoutRequest.Response(true, new LogoutRequest.Data(revoked));
    }

    @Transactional(readOnly = true)
    public AuthResponse refresh(RefreshRequest request) {
        String accessToken = generateToken();
        String refreshToken = generateToken();
        return new AuthResponse(true, new AuthResponse.TokenData(accessToken, refreshToken, "Bearer", 3600, null));
    }

    @Transactional
    public AuthResponse socialLogin(SocialLoginRequest request) {
        String email = request.provider().name().toLowerCase() + "_user@" + request.provider().name().toLowerCase() + ".com";
        User user = userRepository.findByEmail(email)
                .orElseGet(() -> createSocialUser(email, request.provider().name() + "유저"));
        String accessToken = generateToken();
        String refreshToken = generateToken();
        return AuthResponse.from(accessToken, refreshToken, user, user.getPreferences());
    }

    private User createUserFromRegister(RegisterRequest request) {
        LocalDateTime now = LocalDateTime.now();
        User user = User.builder()
                .email(request.email())
                .password(request.password())
                .nickname(request.nickname())
                .name(request.name())
                .gender(request.gender())
                .birthday(LocalDate.parse(request.birthday()))
                .createdAt(now)
                .updatedAt(now)
                .build();
        return userRepository.save(user);
    }

    private User createSocialUser(String email, String nickname) {
        LocalDateTime now = LocalDateTime.now();
        User user = User.builder()
                .email(email)
                .password(generateToken())
                .nickname(nickname)
                .createdAt(now)
                .updatedAt(now)
                .build();
        return userRepository.save(user);
    }

    private String generateToken() {
        return UUID.randomUUID().toString();
    }
}
