package com.nutrigo.nutrigo_backend.domain.insight;

import com.nutrigo.nutrigo_backend.domain.insight.dto.InsightLogRequest;
import com.nutrigo.nutrigo_backend.domain.insight.dto.InsightLogResponse;
import com.nutrigo.nutrigo_backend.domain.insight.dto.MealAnalysisResult;
import com.nutrigo.nutrigo_backend.domain.insight.dto.NutrientProfile;
import com.nutrigo.nutrigo_backend.domain.user.User;
import com.nutrigo.nutrigo_backend.domain.user.UserRepository;
import com.nutrigo.nutrigo_backend.global.common.enums.MealTime;
import com.nutrigo.nutrigo_backend.global.error.AppExceptions.User.UserNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class InsightLogService {

    private final MealLogRepository mealLogRepository;
    private final UserRepository userRepository;
    private final DailyIntakeSummaryRepository dailyIntakeSummaryRepository;
    private final NutritionScoreService nutritionScoreService;
    private final MealAnalysisClient mealAnalysisClient;

    @Transactional
    public InsightLogResponse logInsight(InsightLogRequest request) {
        User user = getCurrentUser();

        MealAnalysisResult analysisResult = mealAnalysisClient.analyze(request);
        NutrientProfile nutrientProfile = Optional.ofNullable(analysisResult)
                .map(MealAnalysisResult::nutrientProfile)
                .orElse(null);

        Float mealScore = nutritionScoreService.calculateMealScore(user, nutrientProfile);
        Float totalScore = analysisResult != null && analysisResult.totalScore() != null
                ? analysisResult.totalScore()
                : mealScore;

        DailyIntakeSummary summary = upsertDailyIntakeSummary(user, request, nutrientProfile, mealScore);

        MealLog mealLog = MealLog.builder()
                .menu(analysisResult != null && analysisResult.menu() != null ? analysisResult.menu() : request.menu())
                .category(analysisResult != null ? analysisResult.category() : null)
                .kcal(nutrientProfile != null ? nutrientProfile.kcal() : null)
                .sodiumMg(nutrientProfile != null ? nutrientProfile.sodiumMg() : null)
                .proteinG(nutrientProfile != null ? nutrientProfile.proteinG() : null)
                .carbG(nutrientProfile != null ? nutrientProfile.carbG() : null)
                .totalScore(totalScore)
                .mealTime(request.mealtime())
                .mealDate(request.mealDate())
                .createdAt(LocalDateTime.now())
                .dailyIntakeSummary(summary)
                .build();

        MealLog saved = mealLogRepository.save(mealLog);

        InsightLogResponse.Data data = new InsightLogResponse.Data(saved.getId(), Collections.emptyList());
        return new InsightLogResponse(true, data);
    }

    private DailyIntakeSummary upsertDailyIntakeSummary(User user, InsightLogRequest request, NutrientProfile nutrientProfile, Float mealScore) {
        LocalDate mealDate = request.mealDate();
        LocalDateTime now = LocalDateTime.now();
        DailyIntakeSummary summary = dailyIntakeSummaryRepository
                .findByUserAndDate(user, mealDate)
                .orElse(null);

        boolean isNewSummary = summary == null;
        if (isNewSummary) {
            summary = DailyIntakeSummary.builder()
                    .user(user)
                    .date(mealDate)
                    .createdAt(now)
                    .dayColor("green")
                    .build();
        }

        long previousMeals = isNewSummary ? 0 : mealLogRepository.countByDailyIntakeSummary(summary);

        Float existingKcal = summary.getTotalKcal() != null ? summary.getTotalKcal() : 0f;
        if (nutrientProfile != null && nutrientProfile.kcal() != null) {
            summary.setTotalKcal(existingKcal + nutrientProfile.kcal());
        }

        Float existingSodium = summary.getTotalSodiumMg() != null ? summary.getTotalSodiumMg() : 0f;
        if (nutrientProfile != null && nutrientProfile.sodiumMg() != null) {
            summary.setTotalSodiumMg(existingSodium + nutrientProfile.sodiumMg());
        }

        Float existingProtein = summary.getTotalProteinG() != null ? summary.getTotalProteinG() : 0f;
        if (nutrientProfile != null && nutrientProfile.proteinG() != null) {
            summary.setTotalProteinG(existingProtein + nutrientProfile.proteinG());
        }

        Float existingCarb = summary.getTotalCarbG() != null ? summary.getTotalCarbG() : 0f;
        if (nutrientProfile != null && nutrientProfile.carbG() != null) {
            summary.setTotalCarbG(existingCarb + nutrientProfile.carbG());
        }

        if (mealScore != null) {
            Float existingScore = summary.getDayScore() != null ? summary.getDayScore() : 0f;
            float newScore = previousMeals > 0
                    ? (existingScore * previousMeals + mealScore) / (previousMeals + 1)
                    : mealScore;
            summary.setDayScore(newScore);
            summary.setDayColor(nutritionScoreService.resolveDayColor(newScore));
        }

        Integer snacks = summary.getTotalSnack() != null ? summary.getTotalSnack() : 0;
        Integer nights = summary.getTotalNight() != null ? summary.getTotalNight() : 0;
        if (request.mealtime() == MealTime.SNACK) {
            summary.setTotalSnack(snacks + 1);
        } else if (request.mealtime() == MealTime.NIGHT) {
            summary.setTotalNight(nights + 1);
        }

        summary.setUpdatedAt(now);

        return dailyIntakeSummaryRepository.save(summary);
    }

    private User getCurrentUser() {
        return userRepository.findAll()
                .stream()
                .findFirst()
                .orElseThrow(UserNotFoundException::new);
    }
}