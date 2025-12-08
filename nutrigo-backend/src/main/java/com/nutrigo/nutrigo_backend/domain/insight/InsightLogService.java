package com.nutrigo.nutrigo_backend.domain.insight;

import com.nutrigo.nutrigo_backend.domain.insight.dto.InsightLogRequest;
import com.nutrigo.nutrigo_backend.domain.insight.dto.InsightLogResponse;
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

@Service
@RequiredArgsConstructor
public class InsightLogService {

    private final MealLogRepository mealLogRepository;
    private final UserRepository userRepository;
    private final DailyIntakeSummaryRepository dailyIntakeSummaryRepository;

    @Transactional
    public InsightLogResponse logInsight(InsightLogRequest request) {
        User user = getCurrentUser();
        DailyIntakeSummary summary = upsertDailyIntakeSummary(user, request);

        MealLog mealLog = MealLog.builder()
                .menu(request.menu())
                .kcal(request.kcal())
                .sodiumMg(request.sodiumMg())
                .proteinG(request.proteinG())
                .carbG(request.carbG())
                .totalScore(request.totalScore())
                .mealTime(request.mealtime())
                .mealDate(request.mealDate())
                .createdAt(LocalDateTime.now())
                .dailyIntakeSummary(summary)
                .build();

        MealLog saved = mealLogRepository.save(mealLog);

        InsightLogResponse.Data data = new InsightLogResponse.Data(saved.getId(), Collections.emptyList());
        return new InsightLogResponse(true, data);
    }

    private DailyIntakeSummary upsertDailyIntakeSummary(User user, InsightLogRequest request) {
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
        if (request.kcal() != null) {
            summary.setTotalKcal(existingKcal + request.kcal());
        }

        Float existingSodium = summary.getTotalSodiumMg() != null ? summary.getTotalSodiumMg() : 0f;
        if (request.sodiumMg() != null) {
            summary.setTotalSodiumMg(existingSodium + request.sodiumMg());
        }

        Float existingProtein = summary.getTotalProteinG() != null ? summary.getTotalProteinG() : 0f;
        if (request.proteinG() != null) {
            summary.setTotalProteinG(existingProtein + request.proteinG());
        }

        Float existingCarb = summary.getTotalCarbG() != null ? summary.getTotalCarbG() : 0f;
        if (request.carbG() != null) {
            summary.setTotalCarbG(existingCarb + request.carbG());
        }

        Float mealScore = request.totalScore();
        if (mealScore != null) {
            Float existingScore = summary.getDayScore() != null ? summary.getDayScore() : 0f;
            float newScore = previousMeals > 0
                    ? (existingScore * previousMeals + mealScore) / (previousMeals + 1)
                    : mealScore;
            summary.setDayScore(newScore);
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