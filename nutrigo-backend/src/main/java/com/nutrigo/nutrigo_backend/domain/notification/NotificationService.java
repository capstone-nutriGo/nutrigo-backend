package com.nutrigo.nutrigo_backend.domain.notification;

import com.nutrigo.nutrigo_backend.domain.insight.DailyIntakeSummary;
import com.nutrigo.nutrigo_backend.domain.insight.DailyIntakeSummaryRepository;
import com.nutrigo.nutrigo_backend.domain.insight.MealLogRepository;
import com.nutrigo.nutrigo_backend.domain.user.User;
import com.nutrigo.nutrigo_backend.domain.user.UserRepository;
import com.nutrigo.nutrigo_backend.domain.user.UserSetting;
import com.nutrigo.nutrigo_backend.domain.user.UserSettingRepository;
import com.nutrigo.nutrigo_backend.domain.challenge.Challenge;
import com.nutrigo.nutrigo_backend.domain.challenge.UserChallenge;
import com.nutrigo.nutrigo_backend.domain.challenge.UserChallengeRepository;
import com.nutrigo.nutrigo_backend.global.common.enums.ChallengeType;
import com.nutrigo.nutrigo_backend.global.security.AuthenticatedUserProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final UserRepository userRepository;
    private final UserSettingRepository userSettingRepository;
    private final DailyIntakeSummaryRepository dailyIntakeSummaryRepository;
    private final MealLogRepository mealLogRepository;
    private final UserChallengeRepository userChallengeRepository;
    private final AuthenticatedUserProvider authenticatedUserProvider;

    /**
     * 식단 코치 알림 전송 (모든 끼니)
     * 아침(6-11시), 점심(11-15시), 저녁(17-21시) 시간대에 알림 전송
     */
    @Transactional(readOnly = true)
    public NotificationResult sendMealCoachNotifications() {
        LocalTime now = LocalTime.now();
        int currentHour = now.getHour();
        
        String mealTime;
        LocalDate targetDate;
        
        // 시간대에 따라 확인할 식사 결정
        if (currentHour >= 6 && currentHour < 11) {
            // 아침 시간대: 전날 저녁 확인
            mealTime = "DINNER";
            targetDate = LocalDate.now().minusDays(1);
        } else if (currentHour >= 11 && currentHour < 15) {
            // 점심 시간대: 당일 아침 확인
            mealTime = "BREAKFAST";
            targetDate = LocalDate.now();
        } else if (currentHour >= 17 && currentHour < 21) {
            // 저녁 시간대: 당일 점심 확인
            mealTime = "LUNCH";
            targetDate = LocalDate.now();
        } else {
            // 알림 시간대가 아님
            return new NotificationResult(0, 0, "알림 시간대가 아닙니다.");
        }

        List<User> users = userRepository.findAll();
        int sentCount = 0;
        int skippedCount = 0;

        for (User user : users) {
            UserSetting settings = userSettingRepository.findById(user.getId()).orElse(null);
            
            // 식단 코치 알림이 꺼져있으면 스킵
            if (settings == null || !Boolean.TRUE.equals(settings.getEveningCoach())) {
                skippedCount++;
                continue;
            }

            // 해당 날짜의 식사 데이터 확인
            DailyIntakeSummary summary = dailyIntakeSummaryRepository
                    .findByUserAndDate(user, targetDate)
                    .orElse(null);

            if (summary == null) {
                skippedCount++;
                continue;
            }

            // 해당 시간대의 식사가 있는지 확인
            long mealCount = mealLogRepository.findAllByDailyIntakeSummary_UserAndMealDate(user, targetDate)
                    .stream()
                    .filter(meal -> meal.getMealTime().name().equals(mealTime))
                    .count();

            if (mealCount == 0) {
                skippedCount++;
                continue;
            }

            // 영양 정보 분석
            Float totalKcal = summary.getTotalKcal();
            Float totalSodiumMg = summary.getTotalSodiumMg();

            if (totalKcal == null || totalSodiumMg == null) {
                skippedCount++;
                continue;
            }

            // 기준값 설정
            float highCalorieThreshold = 2500f; // 하루 권장 칼로리
            float highSodiumThreshold = 2000f; // 하루 권장 나트륨 (mg)

            // 알림 조건 확인 (나트륨이나 칼로리가 높을 때만)
            if (totalSodiumMg > highSodiumThreshold || totalKcal > highCalorieThreshold) {
                log.info("[NotificationService] 식단 코치 알림 전송: userId={}, mealTime={}, date={}", 
                        user.getId(), mealTime, targetDate);
                sentCount++;
                // 실제로는 여기서 푸시 알림이나 이메일을 전송
                // 현재는 로그만 남김
            } else {
                skippedCount++;
            }
        }

        return new NotificationResult(sentCount, skippedCount, 
                String.format("식단 코치 알림: %d명 전송, %d명 스킵", sentCount, skippedCount));
    }

    /**
     * 챌린지 리마인드 알림 전송
     */
    @Transactional(readOnly = true)
    public NotificationResult sendChallengeReminders() {
        List<User> users = userRepository.findAll();
        int sentCount = 0;
        int skippedCount = 0;

        for (User user : users) {
            UserSetting settings = userSettingRepository.findById(user.getId()).orElse(null);
            
            // 챌린지 리마인드가 꺼져있으면 스킵
            if (settings == null || !Boolean.TRUE.equals(settings.getChallengeReminder())) {
                skippedCount++;
                continue;
            }

            // 진행 중인 챌린지 확인
            List<UserChallenge> activeChallenges = userChallengeRepository.findByUser(user)
                    .stream()
                    .filter(uc -> "ongoing".equalsIgnoreCase(uc.getStatus()))
                    .toList();

            if (activeChallenges.isEmpty()) {
                skippedCount++;
                continue;
            }

            log.info("[NotificationService] 챌린지 리마인드 알림 전송: userId={}, challengeCount={}", 
                    user.getId(), activeChallenges.size());
            sentCount++;
            // 실제로는 여기서 푸시 알림이나 이메일을 전송
            // 현재는 로그만 남김
        }

        return new NotificationResult(sentCount, skippedCount, 
                String.format("챌린지 리마인드 알림: %d명 전송, %d명 스킵", sentCount, skippedCount));
    }

    /**
     * 테스트용: 현재 사용자에게 식단 코치 알림 전송
     */
    @Transactional(readOnly = true)
    public NotificationResult testMealCoachNotification() {
        User user = authenticatedUserProvider.getCurrentUser();
        LocalTime now = LocalTime.now();
        int currentHour = now.getHour();
        
        String mealTime;
        LocalDate targetDate;
        String timeContext;
        
        // 시간대에 따라 확인할 식사 결정
        if (currentHour >= 6 && currentHour < 11) {
            mealTime = "DINNER";
            targetDate = LocalDate.now().minusDays(1);
            timeContext = "어제 저녁";
        } else if (currentHour >= 11 && currentHour < 15) {
            mealTime = "BREAKFAST";
            targetDate = LocalDate.now();
            timeContext = "오늘 아침";
        } else if (currentHour >= 17 && currentHour < 21) {
            mealTime = "LUNCH";
            targetDate = LocalDate.now();
            timeContext = "오늘 점심";
        } else {
            // 알림 시간대가 아니어도 테스트를 위해 가장 최근 식사 확인
            mealTime = "DINNER";
            targetDate = LocalDate.now().minusDays(1);
            timeContext = "어제 저녁";
        }

        DailyIntakeSummary summary = dailyIntakeSummaryRepository
                .findByUserAndDate(user, targetDate)
                .orElse(null);

        if (summary == null) {
            return new NotificationResult(0, 1, "해당 날짜의 식사 기록이 없습니다.");
        }

        Float totalKcal = summary.getTotalKcal();
        Float totalSodiumMg = summary.getTotalSodiumMg();

        if (totalKcal == null || totalSodiumMg == null) {
            return new NotificationResult(0, 1, "영양 정보가 없습니다.");
        }

        log.info("[NotificationService] 테스트 식단 코치 알림: userId={}, mealTime={}, date={}, kcal={}, sodium={}", 
                user.getId(), mealTime, targetDate, totalKcal, totalSodiumMg);

        // 실제 알림 메시지 생성
        String notificationMessage = generateMealCoachMessage(timeContext, totalKcal, totalSodiumMg, currentHour);

        return new NotificationResult(1, 0, notificationMessage);
    }

    /**
     * 테스트용: 현재 사용자에게 챌린지 리마인드 알림 전송
     */
    @Transactional(readOnly = true)
    public NotificationResult testChallengeReminder() {
        User user = authenticatedUserProvider.getCurrentUser();
        
        List<UserChallenge> activeChallenges = userChallengeRepository.findByUser(user)
                .stream()
                .filter(uc -> "ongoing".equalsIgnoreCase(uc.getStatus()))
                .toList();

        if (activeChallenges.isEmpty()) {
            return new NotificationResult(0, 1, "진행 중인 챌린지가 없습니다.");
        }

        log.info("[NotificationService] 테스트 챌린지 리마인드 알림: userId={}, challengeCount={}", 
                user.getId(), activeChallenges.size());

        // 실제 알림 메시지 생성
        String notificationMessage = generateChallengeReminderMessage(activeChallenges);

        return new NotificationResult(1, 0, notificationMessage);
    }

    /**
     * 식단 코치 알림 메시지 생성
     */
    private String generateMealCoachMessage(String timeContext, float totalKcal, float totalSodiumMg, int currentHour) {
        float highCalorieThreshold = 2500f;
        float highSodiumThreshold = 2000f;
        
        StringBuilder message = new StringBuilder();
        
        if (totalSodiumMg > highSodiumThreshold) {
            message.append(String.format("🧂 %s이(가) 조금 짜셨네요!\n", timeContext));
            message.append("오늘은 나트륨이 낮은 메뉴로 몸을 쉬게 해주면 좋을 것 같아요.\n\n");
            message.append("💡 추천 메뉴:\n");
            message.append("• 샐러드\n");
            message.append("• 닭가슴살 덮밥\n");
            message.append("• 과일\n");
            message.append("• 요거트");
        } else if (totalKcal > highCalorieThreshold) {
            message.append(String.format("😅 %s이(가) 조금 무거웠어요!\n", timeContext));
            if (currentHour >= 17) {
                message.append("저녁은 조금 가볍게 드셔보는 건 어떨까요?\n\n");
                message.append("💡 추천 메뉴:\n");
                message.append("• 국밥\n");
                message.append("• 비빔밥\n");
                message.append("• 샐러드\n");
                message.append("• 죽");
            } else if (currentHour >= 11 && currentHour < 15) {
                message.append("오늘 점심은 튀김보다는 국/덮밥 위주로 가볍게 먹어보는 건 어떨까요?\n\n");
                message.append("💡 추천 메뉴:\n");
                message.append("• 국밥\n");
                message.append("• 비빔밥\n");
                message.append("• 샐러드\n");
                message.append("• 샌드위치");
            } else {
                message.append("오늘은 가벼운 식사로 몸을 쉬게 해주면 좋을 것 같아요.\n\n");
                message.append("💡 추천 메뉴:\n");
                message.append("• 샐러드\n");
                message.append("• 닭가슴살 덮밥\n");
                message.append("• 과일\n");
                message.append("• 요거트");
            }
        } else {
            message.append(String.format("✅ %s 식사가 적정했어요!\n", timeContext));
            message.append("오늘도 좋은 식습관을 유지하고 계시네요. 계속 이렇게 건강하게 식사하세요! 🎉");
        }
        
        return message.toString();
    }

    /**
     * 챌린지 리마인드 알림 메시지 생성
     */
    private String generateChallengeReminderMessage(List<UserChallenge> activeChallenges) {
        StringBuilder message = new StringBuilder();
        
        message.append("🏆 챌린지 진행 상황 알림\n\n");
        message.append(String.format("현재 진행 중인 챌린지가 %d개 있어요!\n\n", activeChallenges.size()));
        
        LocalDate today = LocalDate.now();
        
        for (int i = 0; i < Math.min(activeChallenges.size(), 3); i++) {
            UserChallenge enrollment = activeChallenges.get(i);
            String title = enrollment.getChallenge().getTitle();
            
            // 실시간으로 진행률 계산 (ChallengeService와 동일한 로직)
            int progressRate = calculateChallengeProgressRate(enrollment, today);
            
            message.append(String.format("• %s: %d%% 진행 중\n", title, progressRate));
        }
        
        if (activeChallenges.size() > 3) {
            message.append(String.format("• 외 %d개 챌린지 진행 중\n", activeChallenges.size() - 3));
        }
        
        message.append("\n💪 오늘도 화이팅!");
        
        return message.toString();
    }

    /**
     * 챌린지 진행률 계산 (ChallengeService의 calculateProgressRate와 동일한 로직)
     */
    private int calculateChallengeProgressRate(UserChallenge enrollment, LocalDate today) {
        Challenge challenge = enrollment.getChallenge();
        Integer durationDays = challenge.getDurationDays();
        
        if (durationDays == null || durationDays <= 0) {
            return enrollment.getProgressRate() != null ? enrollment.getProgressRate().intValue() : 0;
        }
        
        LocalDate startDate = enrollment.getStartedAt() != null ? enrollment.getStartedAt() : today;
        long elapsedDays = ChronoUnit.DAYS.between(startDate, today) + 1;
        long targetDays = Math.min(elapsedDays, durationDays);
        
        if (targetDays <= 0) {
            return 0;
        }
        
        // 챌린지 조건을 만족하는 날짜만 필터링
        List<DailyIntakeSummary> summaries = loadDailyIntakesForChallenge(enrollment, today);
        List<DailyIntakeSummary> validSummaries = filterValidDaysForChallenge(challenge, summaries);
        
        float completionRatio = (float) Math.min(validSummaries.size(), targetDays) / (float) durationDays;
        return Math.round(completionRatio * 100);
    }

    /**
     * 챌린지용 일일 섭취 데이터 로드
     */
    private List<DailyIntakeSummary> loadDailyIntakesForChallenge(UserChallenge enrollment, LocalDate today) {
        LocalDate startDate = enrollment.getStartedAt() != null ? enrollment.getStartedAt() : today;
        LocalDate endDate = enrollment.getEndedAt() != null ? enrollment.getEndedAt() : today;
        LocalDate upperBound = endDate.isBefore(today) ? endDate : today;
        return dailyIntakeSummaryRepository.findAllByUserAndDateBetween(
                enrollment.getUser(),
                startDate,
                upperBound
        );
    }

    /**
     * 챌린지 조건을 만족하는 날짜만 필터링 (ChallengeService의 filterValidDays와 동일한 로직)
     */
    private List<DailyIntakeSummary> filterValidDaysForChallenge(Challenge challenge, List<DailyIntakeSummary> summaries) {
        ChallengeType type = challenge.getType();
        String title = challenge.getTitle();
        
        return summaries.stream()
                .filter(summary -> {
                    if (type == ChallengeType.kcal) {
                        Integer targetKcal = extractKcalFromTitle(title);
                        if (targetKcal != null && summary.getTotalKcal() != null) {
                            return summary.getTotalKcal().intValue() <= targetKcal;
                        }
                        return false;
                    } else if (type == ChallengeType.sodium) {
                        Integer targetSodium = extractSodiumFromTitle(title);
                        if (targetSodium != null && summary.getTotalSodiumMg() != null) {
                            return summary.getTotalSodiumMg().intValue() <= targetSodium;
                        }
                        return false;
                    } else if (type == ChallengeType.protein) {
                        Integer targetProtein = extractProteinFromTitle(title);
                        if (targetProtein != null && summary.getTotalProteinG() != null) {
                            return summary.getTotalProteinG().intValue() >= targetProtein;
                        }
                        return false;
                    } else if (type == ChallengeType.frequency) {
                        return true;
                    } else if (type == ChallengeType.day_color) {
                        return true;
                    }
                    return false;
                })
                .toList();
    }

    /**
     * 제목에서 칼로리 목표 추출
     */
    private Integer extractKcalFromTitle(String title) {
        if (title == null) return null;
        Pattern pattern = Pattern.compile("(\\d+)kcal");
        Matcher matcher = pattern.matcher(title);
        if (matcher.find()) {
            try {
                return Integer.parseInt(matcher.group(1));
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    /**
     * 제목에서 나트륨 목표 추출
     */
    private Integer extractSodiumFromTitle(String title) {
        if (title == null) return null;
        Pattern pattern = Pattern.compile("나트륨\\s*(\\d+)mg|(\\d+)mg");
        Matcher matcher = pattern.matcher(title);
        if (matcher.find()) {
            try {
                String value = matcher.group(1) != null ? matcher.group(1) : matcher.group(2);
                return Integer.parseInt(value);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    /**
     * 제목에서 단백질 목표 추출
     */
    private Integer extractProteinFromTitle(String title) {
        if (title == null) return null;
        Pattern pattern = Pattern.compile("단백질\\s*(\\d+)g|(\\d+)g");
        Matcher matcher = pattern.matcher(title);
        if (matcher.find()) {
            try {
                String value = matcher.group(1) != null ? matcher.group(1) : matcher.group(2);
                return Integer.parseInt(value);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    public record NotificationResult(int sentCount, int skippedCount, String message) {
    }
}

