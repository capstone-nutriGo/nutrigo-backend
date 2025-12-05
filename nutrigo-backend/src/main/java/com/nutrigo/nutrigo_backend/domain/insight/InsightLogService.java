package com.nutrigo.nutrigo_backend.domain.insight;

import com.nutrigo.nutrigo_backend.domain.insight.dto.InsightLogRequest;
import com.nutrigo.nutrigo_backend.domain.insight.dto.InsightLogResponse;
import com.nutrigo.nutrigo_backend.global.error.AppExceptions.Insight.AnalysisSessionNotFoundException;
import com.nutrigo.nutrigo_backend.global.common.enums.MealTime;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.Collections;

@Service
@RequiredArgsConstructor
public class InsightLogService {

    private final MealLogRepository mealLogRepository;
    private final AnalysisSessionRepository analysisSessionRepository;
    private final DailyIntakeSummaryRepository dailyIntakeSummaryRepository;

    @Transactional
    public InsightLogResponse logInsight(InsightLogRequest request) {
        AnalysisSession analysisSession = analysisSessionRepository.findById(request.analysisId())
                .orElseThrow(() -> new AnalysisSessionNotFoundException(request.analysisId()));
        DailyIntakeSummary summary = upsertDailyIntakeSummary(analysisSession, request.mealtime(), request.orderedAt());

        MealLog mealLog = MealLog.builder()
                .mealTime(request.mealtime())
                .mealDate(request.orderedAt().toLocalDate())
                .createdAt(LocalDateTime.now())
                .dailyIntakeSummary(summary)
                .build();

        MealLog saved = mealLogRepository.save(mealLog);

        InsightLogResponse.Data data = new InsightLogResponse.Data(saved.getId(), Collections.emptyList());
        return new InsightLogResponse(true, data);
    }

    private DailyIntakeSummary upsertDailyIntakeSummary(AnalysisSession analysisSession, MealTime mealTime, OffsetDateTime orderedAt) {
        LocalDateTime now = LocalDateTime.now();
        DailyIntakeSummary summary = dailyIntakeSummaryRepository
                .findByUserAndDate(analysisSession.getUser(), orderedAt.toLocalDate())
                .orElseGet(() -> DailyIntakeSummary.builder()
                        .user(analysisSession.getUser())
                        .date(orderedAt.toLocalDate())
                        .createdAt(now)
                        .updatedAt(now)
                        .dayColor("green")
                        .build());

        long previousMeals = mealLogRepository.countByDailyIntakeSummary(summary);

        Float existingKcal = summary.getTotalKcal() != null ? summary.getTotalKcal() : 0f;
        if (analysisSession.getTotalKcal() != null) {
            summary.setTotalKcal(existingKcal + analysisSession.getTotalKcal());
        }

        Float existingSodium = summary.getTotalSodiumMg() != null ? summary.getTotalSodiumMg() : 0f;
        if (analysisSession.getTotalSodiumMg() != null) {
            summary.setTotalSodiumMg(existingSodium + analysisSession.getTotalSodiumMg());
        }

        Float sessionScore = analysisSession.getTotalScore();
        if (sessionScore != null) {
            Float existingScore = summary.getDayScore() != null ? summary.getDayScore() : 0f;
            float newScore = previousMeals > 0
                    ? (existingScore * previousMeals + sessionScore) / (previousMeals + 1)
                    : sessionScore;
            summary.setDayScore(newScore);
        }

        Integer snacks = summary.getTotalSnack() != null ? summary.getTotalSnack() : 0;
        Integer nights = summary.getTotalNight() != null ? summary.getTotalNight() : 0;
        if (mealTime == MealTime.SNACK) {
            summary.setTotalSnack(snacks + 1);
        } else if (mealTime == MealTime.NIGHT) {
            summary.setTotalNight(nights + 1);
        }

        summary.setUpdatedAt(now);

        return dailyIntakeSummaryRepository.save(summary);    }
}