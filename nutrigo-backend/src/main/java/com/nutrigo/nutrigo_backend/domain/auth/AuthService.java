package com.nutrigo.nutrigo_backend.domain.auth;

import com.nutrigo.nutrigo_backend.domain.auth.dto.AuthResponse;
import com.nutrigo.nutrigo_backend.domain.auth.dto.LoginRequest;
import com.nutrigo.nutrigo_backend.domain.auth.dto.LogoutRequest;
import com.nutrigo.nutrigo_backend.domain.auth.dto.RefreshRequest;
import com.nutrigo.nutrigo_backend.domain.auth.dto.RegisterRequest;
import com.nutrigo.nutrigo_backend.domain.auth.dto.SocialLoginRequest;
import com.nutrigo.nutrigo_backend.global.error.AppExceptions.Auth.DuplicateEmailException;
import com.nutrigo.nutrigo_backend.global.error.AppExceptions.Auth.InvalidCredentialsException;
import com.nutrigo.nutrigo_backend.global.error.AppExceptions.Auth.UserNotFoundException;
import com.nutrigo.nutrigo_backend.global.common.enums.Gender;
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
        // 1) 이메일 중복 검사
        if (userRepository.existsByEmail(request.email())) {
            throw new DuplicateEmailException();
        }

        // 2) 새 유저 생성
        User user = createUserFromRegister(request);

        // 3) userId 기반 토큰 생성 (feature/user-api 방식 유지)
        String accessToken = generateToken(user.getId());
        String refreshToken = generateToken(user.getId());

        return AuthResponse.from(accessToken, refreshToken, user, null);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
      
        // 1) 이메일로 사용자 조회 (없으면 404 + UserNotFoundException)
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(UserNotFoundException::new);

        // 2) 비밀번호 검증 (틀리면 401 + InvalidCredentialsException)
        if (user.getPassword() == null || !user.getPassword().equals(request.password())) {
            throw new InvalidCredentialsException();
        }

        // 3) userId 기반 토큰 생성 (access/refresh 둘 다)
        String accessToken = generateToken(user.getId());
        String refreshToken = generateToken(user.getId());

        return AuthResponse.from(accessToken, refreshToken, user, user.getPreferences());
    }

    public LogoutRequest.Response logout(String authorization, LogoutRequest request) {
        boolean revoked = authorization != null && authorization.toLowerCase().startsWith("bearer");
        return new LogoutRequest.Response(true, new LogoutRequest.Data(revoked));
    }

    @Transactional(readOnly = true)
    public AuthResponse refresh(RefreshRequest request) {
        // refresh 토큰에서 사용자 ID 추출
        Long userId = extractUserIdFromToken(request.refreshToken());
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid refresh token"));

        String accessToken = generateToken(userId);
        String refreshToken = generateToken(userId);
        return AuthResponse.from(accessToken, refreshToken, user, user.getPreferences());
    }

    private Long extractUserIdFromToken(String token) {
        // 토큰 형식: "userId:uuid" (예: "1:a1b2c3d4-e5f6-7890-abcd-ef1234567890")
        if (token != null && token.contains(":")) {
            String userIdStr = token.split(":")[0];
            try {
                return Long.parseLong(userIdStr);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid token format");
            }
        }
        throw new IllegalArgumentException("Invalid token format");
    }

    @Transactional
    public AuthResponse socialLogin(SocialLoginRequest request) {
        return socialLogin(request, null, null);
    }

    @Transactional
    public AuthResponse socialLogin(SocialLoginRequest request, String emailOverride, String nicknameOverride) {
        String provider = request.provider().name().toLowerCase();
        String email = emailOverride != null ? emailOverride : provider + "_user@" + provider + ".com";
        String nickname = nicknameOverride != null ? nicknameOverride : request.provider().name() + "유저";

        User user = userRepository.findByEmail(email)
                .orElseGet(() -> createSocialUser(email, nickname));
        String accessToken = generateToken(user.getId());
        String refreshToken = generateToken(user.getId());
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
                .birthday(request.birthday())
                .createdAt(now)
                .updatedAt(now)
                .build();
        return userRepository.save(user);
    }

    private User createSocialUser(String email, String nickname) {
        LocalDateTime now = LocalDateTime.now();
        String safeNickname = nickname != null ? nickname : "소셜 유저";
        String safeName = safeNickname;
        User user = User.builder()
                .email(truncate(email, 100))
                .password(UUID.randomUUID().toString()) // password는 UUID만 사용
                .nickname(truncate(safeNickname, 50))
                .name(truncate(safeName, 30))
                .gender(Gender.other)
                .birthday(User.SOCIAL_PLACEHOLDER_BIRTHDAY)
                .createdAt(now)
                .updatedAt(now)
                .build();
        return userRepository.save(user);
    }

    private String truncate(String value, int maxLength) {
        if (value == null) {
            return null;
        }
        return value.length() <= maxLength ? value : value.substring(0, maxLength);
    }

    private String generateToken(Long userId) {
        // 토큰 형식: userId:uuid (예: "1:a1b2c3d4-e5f6-7890-abcd-ef1234567890")
        return userId + ":" + UUID.randomUUID().toString();
    }

    private String generateToken() {
        // refresh용 (사용자 ID 없이)
        return UUID.randomUUID().toString();
    }
}