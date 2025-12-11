package com.nutrigo.nutrigo_backend.domain.nutribot;

import com.nutrigo.nutrigo_backend.domain.insight.DailyIntakeSummary;
import com.nutrigo.nutrigo_backend.domain.insight.DailyIntakeSummaryRepository;
import com.nutrigo.nutrigo_backend.domain.insight.MealLog;
import com.nutrigo.nutrigo_backend.domain.insight.MealLogRepository;
import com.nutrigo.nutrigo_backend.domain.nutribot.dto.*;
import com.nutrigo.nutrigo_backend.domain.nutrition.NutrigoAiClient;
import com.nutrigo.nutrigo_backend.domain.user.User;
import com.nutrigo.nutrigo_backend.domain.user.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class NutriBotService {

    private final NutrigoAiClient nutrigoAiClient;
    private final UserService userService;
    private final DailyIntakeSummaryRepository dailyIntakeSummaryRepository;
    private final MealLogRepository mealLogRepository;

    @Transactional(readOnly = true)
    public NutriBotCoachResponse chat(String userMessage, String authorization) {
        try {
            log.info("[NutriBotService] 챗봇 요청 시작: userMessage={}", userMessage);
            
            User user = userService.getCurrentUser(authorization);
            log.info("[NutriBotService] 사용자 조회 완료: userId={}", user.getId());

            // 최근 7일간의 일일 요약 가져오기
            LocalDate endDate = LocalDate.now();
            LocalDate startDate = endDate.minusDays(6);
            List<DailyIntakeSummary> summaries = dailyIntakeSummaryRepository
                    .findAllByUserAndDateBetween(user, startDate, endDate);
            log.info("[NutriBotService] 일일 요약 조회 완료: count={}", summaries.size());

            // 최근 7일간의 식사 기록 가져오기 (최대 10개)
            // DailyIntakeSummary를 통해 사용자별 식사 기록 필터링
            List<MealLog> recentMeals = summaries.stream()
                    .flatMap(summary -> {
                        // summary에 연결된 mealLog들을 가져오기 위해 summary의 ID를 사용
                        return mealLogRepository.findAllByMealDate(summary.getDate()).stream()
                                .filter(mealLog -> mealLog.getDailyIntakeSummary() != null 
                                        && mealLog.getDailyIntakeSummary().getId().equals(summary.getId()));
                    })
                    .limit(10)
                    .collect(Collectors.toList());
            log.info("[NutriBotService] 식사 기록 조회 완료: count={}", recentMeals.size());

            // DTO 변환
            List<DailySummaryDto> dailySummaries = summaries.stream()
                    .map(this::toDailySummaryDto)
                    .collect(Collectors.toList());

            List<MenuAnalysisDto> recentMenus = recentMeals.stream()
                    .map(this::toMenuAnalysisDto)
                    .collect(Collectors.toList());

            // 사용자 목표 설정 (기본값)
            UserGoalDto userGoal = UserGoalDto.builder()
                    .focus("maintenance")
                    .calorieMax(2500)
                    .sodiumMax(2300)
                    .proteinMin(50)
                    .build();

            // AI 서비스 호출
            NutriBotCoachRequest request = NutriBotCoachRequest.builder()
                    .mode("chat")
                    .userGoal(userGoal)
                    .dailySummaries(dailySummaries)
                    .recentMenus(recentMenus)
                    .userMessage(userMessage)
                    .build();

            log.info("[NutriBotService] AI 서비스 호출 시작");
            NutriBotCoachResponse response = nutrigoAiClient.coachNutriBot(request);
            log.info("[NutriBotService] AI 서비스 호출 완료: reply={}", response.getReply());
            
            return response;
        } catch (Exception e) {
            log.error("[NutriBotService] 챗봇 처리 중 오류 발생", e);
            throw new RuntimeException("챗봇 응답 생성 실패: " + e.getMessage(), e);
        }
    }

    private DailySummaryDto toDailySummaryDto(DailyIntakeSummary summary) {
        return DailySummaryDto.builder()
                .date(summary.getDate())
                .totalKcal(summary.getTotalKcal() != null ? summary.getTotalKcal().doubleValue() : 0.0)
                .totalSodiumMg(summary.getTotalSodiumMg() != null ? summary.getTotalSodiumMg().doubleValue() : 0.0)
                .totalProteinG(summary.getTotalProteinG() != null ? summary.getTotalProteinG().doubleValue() : 0.0)
                .totalFatG(0.0) // DailyIntakeSummary에 fat 필드가 없으면 0.0
                .totalCarbG(summary.getTotalCarbG() != null ? summary.getTotalCarbG().doubleValue() : 0.0)
                .build();
    }

    private MenuAnalysisDto toMenuAnalysisDto(MealLog mealLog) {
        MenuTextDto menuText = MenuTextDto.builder()
                .id(mealLog.getId().toString())
                .name(mealLog.getMenu())
                .description(mealLog.getCategory())
                .build();

        NutritionEstimateDto nutrition = NutritionEstimateDto.builder()
                .kcal(mealLog.getKcal() != null ? mealLog.getKcal().doubleValue() : 0.0)
                .sodiumMg(mealLog.getSodiumMg() != null ? mealLog.getSodiumMg().doubleValue() : 0.0)
                .proteinG(mealLog.getProteinG() != null ? mealLog.getProteinG().doubleValue() : 0.0)
                .carbG(mealLog.getCarbG() != null ? mealLog.getCarbG().doubleValue() : 0.0)
                .fatG(0.0) // MealLog에 fat 필드가 없으면 0.0
                .confidence(0.8)
                .build();

        return MenuAnalysisDto.builder()
                .menu(menuText)
                .nutrition(nutrition)
                .score(mealLog.getTotalScore() != null ? mealLog.getTotalScore().doubleValue() : 50.0)
                .badges(new ArrayList<>())
                .coachSentence("")
                .build();
    }
}

