package com.nutrigo.nutrigo_backend.domain.insight;

import com.nutrigo.nutrigo_backend.domain.insight.dto.InsightLogRequest;
import com.nutrigo.nutrigo_backend.domain.insight.dto.InsightLogResponse;
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
                .orElseThrow(() -> new IllegalArgumentException("Analysis session not found: " + request.analysisId()));

        MealLog mealLog = MealLog.builder()
                .analysisSession(analysisSession)
                .source(request.source())
                .mealTime(request.mealtime())
                .orderedAt(request.orderedAt())
                .createdAt(LocalDateTime.now())
                .build();

        MealLog saved = mealLogRepository.save(mealLog);

        upsertDailyIntakeSummary(analysisSession, request.orderedAt());

        InsightLogResponse.Data data = new InsightLogResponse.Data(saved.getId(), Collections.emptyList());
        return new InsightLogResponse(true, data);
    }

    private void upsertDailyIntakeSummary(AnalysisSession analysisSession, OffsetDateTime orderedAt) {
        LocalDateTime now = LocalDateTime.now();
        DailyIntakeSummary summary = dailyIntakeSummaryRepository
                .findByUserAndDate(analysisSession.getUser(), orderedAt.toLocalDate())
                .orElseGet(() -> DailyIntakeSummary.builder()
                        .user(analysisSession.getUser())
                        .date(orderedAt.toLocalDate())
                        .createdAt(now)
                        .totalKcal(0f)
                        .totalSodiumMg(0f)
                        .totalProteinG(0f)
                        .totalMeals(0)
                        .build());

        int previousMeals = summary.getTotalMeals() != null ? summary.getTotalMeals() : 0;
        summary.setTotalMeals(previousMeals + 1);

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

        dailyIntakeSummaryRepository.save(summary);
    }
}