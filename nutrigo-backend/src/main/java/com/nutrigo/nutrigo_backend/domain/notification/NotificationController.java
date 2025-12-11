package com.nutrigo.nutrigo_backend.domain.notification;

import com.nutrigo.nutrigo_backend.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    /**
     * POST /api/v1/notifications/test/meal-coach
     * 테스트용: 현재 사용자에게 식단 코치 알림 전송
     */
    @PostMapping("/test/meal-coach")
    public ResponseEntity<ApiResponse<NotificationService.NotificationResult>> testMealCoachNotification() {
        NotificationService.NotificationResult result = notificationService.testMealCoachNotification();
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    /**
     * POST /api/v1/notifications/test/challenge-reminder
     * 테스트용: 현재 사용자에게 챌린지 리마인드 알림 전송
     */
    @PostMapping("/test/challenge-reminder")
    public ResponseEntity<ApiResponse<NotificationService.NotificationResult>> testChallengeReminder() {
        NotificationService.NotificationResult result = notificationService.testChallengeReminder();
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    /**
     * POST /api/v1/notifications/send/meal-coach
     * 관리자용: 모든 사용자에게 식단 코치 알림 전송 (스케줄러에서 호출)
     */
    @PostMapping("/send/meal-coach")
    public ResponseEntity<ApiResponse<NotificationService.NotificationResult>> sendMealCoachNotifications() {
        NotificationService.NotificationResult result = notificationService.sendMealCoachNotifications();
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    /**
     * POST /api/v1/notifications/send/challenge-reminder
     * 관리자용: 모든 사용자에게 챌린지 리마인드 알림 전송 (스케줄러에서 호출)
     */
    @PostMapping("/send/challenge-reminder")
    public ResponseEntity<ApiResponse<NotificationService.NotificationResult>> sendChallengeReminders() {
        NotificationService.NotificationResult result = notificationService.sendChallengeReminders();
        return ResponseEntity.ok(ApiResponse.success(result));
    }
}

