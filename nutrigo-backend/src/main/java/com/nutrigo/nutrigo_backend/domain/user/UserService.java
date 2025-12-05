package com.nutrigo.nutrigo_backend.domain.user;

import com.nutrigo.nutrigo_backend.domain.user.dto.UserProfileResponse;
import com.nutrigo.nutrigo_backend.domain.user.dto.UserProfileUpdateRequest;
import com.nutrigo.nutrigo_backend.domain.user.dto.UserSettingsRequest;
import com.nutrigo.nutrigo_backend.domain.user.dto.UserSettingsResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserPreferencesRepository userPreferencesRepository;

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
        if (request.address() != null) {
            user.setAddress(request.address());
        }
        user.setUpdatedAt(LocalDateTime.now());

        if (request.preferences() != null) {
            UserPreferences preferences = ensurePreferences(user);
            if (request.preferences().healthMode() != null) {
                preferences.setHealthMode(request.preferences().healthMode());
            }
            if (request.preferences().defaultMode() != null) {
                preferences.setDefaultMode(request.preferences().defaultMode());
            }
            preferences.setUpdatedAt(LocalDateTime.now());
            user.setPreferences(preferences);
            userPreferencesRepository.save(preferences);
        }

        userRepository.save(user);
        return UserProfileResponse.from(user);
    }

    @Transactional
    public UserSettingsResponse updateSettings(UserSettingsRequest request, String authorization) {
        User user = getCurrentUser(authorization);
        UserPreferences preferences = ensurePreferences(user);

        if (request.notification() != null) {
            if (request.notification().eveningCoach() != null) {
                preferences.setEveningCoach(request.notification().eveningCoach());
            }
            if (request.notification().challengeReminder() != null) {
                preferences.setChallengeReminder(request.notification().challengeReminder());
            }
        }
        if (request.defaultMode() != null) {
            preferences.setDefaultMode(request.defaultMode());
        }

        preferences.setUpdatedAt(LocalDateTime.now());
        user.setPreferences(preferences);

        userPreferencesRepository.save(preferences);
        userRepository.save(user);

        return UserSettingsResponse.from(
                preferences.getEveningCoach(),
                preferences.getChallengeReminder(),
                preferences.getDefaultMode() != null ? preferences.getDefaultMode().name() : null
        );
    }

    private User getCurrentUser() {
        return userRepository.findAll()
                .stream()
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No users available"));
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

    private UserPreferences ensurePreferences(User user) {
        return userPreferencesRepository.findById(user.getId())
                .orElseGet(() -> UserPreferences.builder()
                        .user(user)
                        .updatedAt(LocalDateTime.now())
                        .build());
    }
}