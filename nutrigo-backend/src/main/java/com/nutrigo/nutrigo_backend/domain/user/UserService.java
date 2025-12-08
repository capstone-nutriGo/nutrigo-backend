package com.nutrigo.nutrigo_backend.domain.user;

import com.nutrigo.nutrigo_backend.domain.user.dto.UserProfileResponse;
import com.nutrigo.nutrigo_backend.domain.user.dto.UserProfileUpdateRequest;
import com.nutrigo.nutrigo_backend.domain.user.dto.UserSettingsRequest;
import com.nutrigo.nutrigo_backend.domain.user.dto.UserSettingsResponse;
import com.nutrigo.nutrigo_backend.global.error.AppExceptions.User.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserSettingRepository userSettingRepository;

    @Transactional(readOnly = true)
    public UserProfileResponse getProfile(String authorization) {
        User user = getCurrentUser(authorization);
        return UserProfileResponse.from(user);
    }

    @Transactional
    public UserProfileResponse updateProfile(UserProfileUpdateRequest request, String authorization) {
        User user = getCurrentUser(authorization);

        if (request.nickname() != null) {
            user.setNickname(request.nickname());
        }
        if (request.name() != null) {
            user.setName(request.name());
        }
        if (request.gender() != null) {
            user.setGender(request.gender());
        }
        if (request.birthday() != null) {
            user.setBirthday(request.birthday());
        }
        user.setUpdatedAt(LocalDateTime.now());

        userRepository.save(user);
        return UserProfileResponse.from(user);
    }

    @Transactional
    public UserSettingsResponse updateSettings(UserSettingsRequest request, String authorization) {
        User user = getCurrentUser(authorization);
        UserSetting preferences = ensurePreferences(user);

        if (request.notification() != null) {
            if (request.notification().eveningCoach() != null) {
                preferences.setEveningCoach(request.notification().eveningCoach());
            }
            if (request.notification().challengeReminder() != null) {
                preferences.setChallengeReminder(request.notification().challengeReminder());
            }
        }

        preferences.setUpdatedAt(LocalDateTime.now());
        user.setPreferences(preferences);

        userSettingRepository.save(preferences);
        userRepository.save(user);

        return UserSettingsResponse.from(
                preferences.getEveningCoach(),
                preferences.getChallengeReminder()
        );
    }

    private User getCurrentUser() {
        return userRepository.findAll()
                .stream()
                .findFirst()
                .orElseThrow(UserNotFoundException::new);
    }

    public User getCurrentUser(String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            throw new IllegalStateException("Authorization header is missing or invalid");
        }

        String token = authorization.substring(7); // "Bearer " 제거
        Long userId = extractUserIdFromToken(token);

        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalStateException("User not found with id: " + userId));
    }

    private Long extractUserIdFromToken(String token) {
        // 토큰 형식: "userId:uuid" (예: "1:a1b2c3d4-e5f6-7890-abcd-ef1234567890")
        if (token.contains(":")) {
            String userIdStr = token.split(":")[0];
            try {
                return Long.parseLong(userIdStr);
            } catch (NumberFormatException e) {
                throw new IllegalStateException("Invalid token format");
            }
        }
        // 기존 형식의 토큰인 경우 (UUID만 있는 경우) - 첫 번째 사용자 반환 (하위 호환성)
        return userRepository.findAll()
                .stream()
                .findFirst()
                .map(User::getId)
                .orElseThrow(() -> new IllegalStateException("No users available"));
    }

    private UserSetting ensurePreferences(User user) {
        return userSettingRepository.findById(user.getId())
                .orElseGet(() -> UserSetting.builder()
                        .user(user)
                        .updatedAt(LocalDateTime.now())
                        .eveningCoach(true)
                        .challengeReminder(true)
                        .build());
    }
}