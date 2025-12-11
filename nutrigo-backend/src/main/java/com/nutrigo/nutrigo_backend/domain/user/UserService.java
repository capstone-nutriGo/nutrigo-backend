package com.nutrigo.nutrigo_backend.domain.user;

import com.nutrigo.nutrigo_backend.domain.user.dto.UserProfileResponse;
import com.nutrigo.nutrigo_backend.domain.user.dto.UserProfileUpdateRequest;
import com.nutrigo.nutrigo_backend.domain.user.dto.UserSettingsRequest;
import com.nutrigo.nutrigo_backend.domain.user.dto.UserSettingsResponse;
import com.nutrigo.nutrigo_backend.global.security.AuthenticatedUserProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserSettingRepository userSettingRepository;
    private final AuthenticatedUserProvider authenticatedUserProvider;

    @Transactional(readOnly = true)
    public UserProfileResponse getProfile() {
        User user = authenticatedUserProvider.getCurrentUser();
        return UserProfileResponse.from(user);
    }

    @Transactional
    public UserProfileResponse updateProfile(UserProfileUpdateRequest request) {
        User user = authenticatedUserProvider.getCurrentUser();

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
    public UserSettingsResponse updateSettings(UserSettingsRequest request) {
        User user = authenticatedUserProvider.getCurrentUser();
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