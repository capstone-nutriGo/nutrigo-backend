package com.nutrigo.nutrigo_backend.domain.user;

import com.nutrigo.nutrigo_backend.domain.user.dto.UserProfileResponse;
import com.nutrigo.nutrigo_backend.domain.user.dto.UserProfileUpdateRequest;
import com.nutrigo.nutrigo_backend.domain.user.dto.UserSettingsRequest;
import com.nutrigo.nutrigo_backend.domain.user.dto.UserSettingsResponse;
import com.nutrigo.nutrigo_backend.global.security.JwtTokenProvider;
import com.nutrigo.nutrigo_backend.global.error.AppExceptions.User.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserSettingRepository userSettingRepository;
    private final JwtTokenProvider jwtTokenProvider;

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

    public User getCurrentUser(String authorization) {
        Long userId = resolveUserId(authorization);
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalStateException("User not found with id: " + userId));
    }

    private Long resolveUserId(String authorization) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && authentication.getPrincipal() instanceof Long principalId) {
            return principalId;
        }

        if (StringUtils.hasText(authorization) && authorization.startsWith("Bearer ")) {
            String token = authorization.substring(7);
            if (jwtTokenProvider.validateToken(token)) {
                return jwtTokenProvider.getUserId(token);
            }
        }

        throw new IllegalStateException("Authorization header is missing or invalid");
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