package com.nutrigo.nutrigo_backend.domain.user;

import com.nutrigo.nutrigo_backend.domain.user.dto.UserProfileResponse;
import com.nutrigo.nutrigo_backend.domain.user.dto.UserProfileUpdateRequest;
import com.nutrigo.nutrigo_backend.domain.user.dto.UserSettingsRequest;
import com.nutrigo.nutrigo_backend.domain.user.dto.UserSettingsResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users/me")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/profile")
    /*
        #swagger.summary = '내 프로필 조회'
        #swagger.description = '현재 인증된 사용자의 기본 정보와 선호도를 조회합니다.'
    */
    public ResponseEntity<UserProfileResponse> getProfile() {
        return ResponseEntity.ok(userService.getProfile());
    }

    @PutMapping("/profile")
    /*
        #swagger.summary = '프로필 수정'
        #swagger.description = '닉네임, 이름, 성별, 생년월일, 주소 및 건강 모드를 업데이트합니다.'
        #swagger.requestBody = {
          required: true,
          content: {
            "application/json": {
              schema: {
                type: "object",
                properties: {
                  nickname: { type: "string", description: "표시될 닉네임" },
                  name: { type: "string", description: "사용자 이름" },
                  gender: { type: "string", description: "성별 (MALE/FEMALE)" },
                  birthday: { type: "string", description: "생년월일 (YYYY-MM-DD)" },
                  address: { type: "string", description: "거주지" },
                  preferences: {
                    type: "object",
                    description: "기본 모드 및 건강 모드 설정",
                    properties: {
                      healthMode: { type: "string", description: "건강 집중 모드" },
                      defaultMode: { type: "string", description: "초기 추천 기준" }
                    }
                  }
                }
              }
            }
          }
        }
    */
    public ResponseEntity<UserProfileResponse> updateProfile(@RequestBody UserProfileUpdateRequest request) {
        return ResponseEntity.ok(userService.updateProfile(request));
    }

    @PutMapping("/settings")
    /*
        #swagger.summary = '알림/환경 설정 변경'
        #swagger.description = '알림 설정과 기본 모드를 변경합니다.'
        #swagger.requestBody = {
          required: true,
          content: {
            "application/json": {
              schema: {
                type: "object",
                properties: {
                  notification: {
                    type: "object",
                    description: "알림 설정",
                    properties: {
                      eveningCoach: { type: "boolean", description: "저녁 코치 알림" },
                      challengeReminder: { type: "boolean", description: "챌린지 리마인더" }
                    }
                  },
                  defaultMode: { type: "string", description: "기본 모드 (예: BALANCED)" }
                },
                required: ["notification"]
              }
            }
          }
        }
    */
    public ResponseEntity<UserSettingsResponse> updateSettings(@RequestBody UserSettingsRequest request) {
        return ResponseEntity.ok(userService.updateSettings(request));
    }
}