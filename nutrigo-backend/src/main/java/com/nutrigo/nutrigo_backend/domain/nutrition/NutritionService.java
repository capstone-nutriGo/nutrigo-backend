package com.nutrigo.nutrigo_backend.domain.nutrition;

import com.nutrigo.nutrigo_backend.domain.insight.DailyIntakeSummary;
import com.nutrigo.nutrigo_backend.domain.insight.DailyIntakeSummaryRepository;
import com.nutrigo.nutrigo_backend.domain.insight.MealLog;
import com.nutrigo.nutrigo_backend.domain.insight.MealLogRepository;
import com.nutrigo.nutrigo_backend.domain.nutrition.dto.*;
import com.nutrigo.nutrigo_backend.domain.user.User;
import com.nutrigo.nutrigo_backend.global.security.AuthenticatedUserProvider;
import com.nutrigo.nutrigo_backend.global.error.AppExceptions;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class NutritionService {

    private final NutrigoAiClient nutrigoAiClient;
    private final ImageUploadService imageUploadService;
    private final MealLogRepository mealLogRepository;
    private final DailyIntakeSummaryRepository dailyIntakeSummaryRepository;
    private final AuthenticatedUserProvider authenticatedUserProvider;

    /**
     * 배달앱 가게 링크 기반 영양 분석
     */
    @Transactional(readOnly = true)
    public NutritionAnalysisResponse analyzeFromStoreLink(StoreLinkAnalysisRequest request) {
        log.info("[NutritionService] store-link 분석 시작: storeUrl={}", request.getStoreUrl());

        NutritionAnalysisResponse response = nutrigoAiClient.analyzeStoreLink(request);

        log.info("[NutritionService] store-link 분석 완료: analyses={}",
                (response != null && response.getAnalyses() != null)
                        ? response.getAnalyses().size()
                        : 0);

        return response;
    }

    /**
     * 업로드용 presigned URL 발급 (generic)
     */
    @Transactional(readOnly = true)
    public ImageUploadUrlResponse createUploadUrl(ImageUploadUrlRequest request) {
        return imageUploadService.createUploadUrl(request);
    }

    /**
     * Cart 이미지 전용 업로드 URL
     */
    @Transactional(readOnly = true)
    public ImageUploadUrlResponse createCartImageUploadUrl(ImageUploadContentTypeRequest request) {
        return imageUploadService.createUploadUrl(UploadType.CART_IMAGE, request.getContentType());
    }

    /**
     * Order 이미지 전용 업로드 URL
     */
    @Transactional(readOnly = true)
    public ImageUploadUrlResponse createOrderImageUploadUrl(ImageUploadContentTypeRequest request) {
        return imageUploadService.createUploadUrl(UploadType.ORDER_IMAGE, request.getContentType());
    }

    /**
     * 장바구니 캡처 이미지 기반 영양 분석
     */
    @Transactional(readOnly = true)
    public NutritionAnalysisResponse analyzeFromCartImage(CartImageAnalysisRequest request) {
        log.info("[NutritionService] cart-image 분석 시작: captureId={}, imageUrl={}",
                request.getCaptureId(), request.getImageUrl());

        NutritionAnalysisResponse response = nutrigoAiClient.analyzeCartImage(request);

        log.info("[NutritionService] cart-image 분석 완료: analyses={}",
                (response != null && response.getAnalyses() != null)
                        ? response.getAnalyses().size()
                        : 0);

        return response;
    }

    /**
     * 주문내역 캡처 기반 식사 기록 후보 + 영양 분석
     *
     * 컨트롤러에서는 OrderImageAnalysisRequest 를 받고,
     * 실제 NutriGo-AI 서버에는 CartImageAnalysisRequest 형태로 전달한다.
     */
    @Transactional
    public OrderImageMealLogResponse analyzeFromOrderImage(OrderImageAnalysisRequest request) {
        log.info("[NutritionService] order-image 분석 시작: captureId={}, imageUrl={}, orderDate={}, mealTime={}",
                request.getCaptureId(), request.getImageUrl(),
                request.getOrderDate(), request.getMealTime());

        // 1) NutriGo-AI 호출 (Python 서버로 order-image 요청)
        OrderImageMealLogResponse response = nutrigoAiClient.analyzeOrderImage(request);

        // 2) 섭취량 슬라이더용 기본 범위 세팅
        applyDefaultIntakeRanges(response);

        // 3) 요청에서 받은 order_date / meal_time 을 응답 DTO에 그대로 실어주기
        if (response != null) {
            response.setOrderDate(request.getOrderDate());
            response.setMealTime(request.getMealTime());
        }

        // 4) MealLog 저장 (다건)
        saveOrderImageMealLogs(response);

        log.info("[NutritionService] order-image 분석 완료: captureId={}, candidateCount={}",
                response != null ? response.getCaptureId() : null,
                (response != null && response.getItems() != null)
                        ? response.getItems().size()
                        : 0);

        return response;
    }


    /**
     * order-image 분석 결과를 MealLog 테이블에 일괄 저장
     */
    @Transactional
    public void saveOrderImageMealLogs(OrderImageMealLogResponse response) {
        if (response == null || response.getItems() == null || response.getItems().isEmpty()) {
            log.warn("[NutritionService] saveOrderImageMealLogs called with empty items");
            return;
        }

        User user = getCurrentUser();
        LocalDate mealDate = response.getOrderDate();
        var mealTime = response.getMealTime();
        LocalDateTime now = LocalDateTime.now();

        DailyIntakeSummary summary = dailyIntakeSummaryRepository
                .findByUserAndDate(user, mealDate)
                .orElseGet(() -> {
                    DailyIntakeSummary newSummary = new DailyIntakeSummary();
                    newSummary.setUser(user);
                    newSummary.setDate(mealDate);
                    newSummary.setCreatedAt(now);
                    newSummary.setUpdatedAt(now);
                    newSummary.setDayColor("green");
                    return dailyIntakeSummaryRepository.save(newSummary);
                });

        long previousMeals = mealLogRepository.countByDailyIntakeSummary(summary);

        final double[] addKcal = {0d};
        final double[] addSodium = {0d};
        final double[] addProtein = {0d};
        final double[] addCarb = {0d};
        final double[] addScoreSum = {0d};
        final int[] scoredCount = {0};

        List<MealLog> logs = response.getItems().stream()
                .map(item -> {
                    if (item.getKcal() != null) addKcal[0] += item.getKcal();
                    if (item.getSodiumMg() != null) addSodium[0] += item.getSodiumMg();
                    if (item.getProteinG() != null) addProtein[0] += item.getProteinG();
                    if (item.getCarbG() != null) addCarb[0] += item.getCarbG();
                    if (item.getTotalScore() != null) {
                        addScoreSum[0] += item.getTotalScore();
                        scoredCount[0]++;
                    }

                    return MealLog.builder()
                            .menu(item.getMenu())
                            .category(item.getCategory())
                            .kcal(item.getKcal())
                            .sodiumMg(item.getSodiumMg())
                            .proteinG(item.getProteinG())
                            .carbG(item.getCarbG())
                            .totalScore(item.getTotalScore())
                            .mealTime(mealTime)
                            .mealDate(mealDate)
                            .createdAt(now)
                            .dailyIntakeSummary(summary)
                            .build();
                })
                .toList();

        summary.setTotalKcal((summary.getTotalKcal() == null ? 0f : summary.getTotalKcal()) + (float) addKcal[0]);
        summary.setTotalSodiumMg((summary.getTotalSodiumMg() == null ? 0f : summary.getTotalSodiumMg()) + (float) addSodium[0]);
        summary.setTotalProteinG((summary.getTotalProteinG() == null ? 0f : summary.getTotalProteinG()) + (float) addProtein[0]);
        summary.setTotalCarbG((summary.getTotalCarbG() == null ? 0f : summary.getTotalCarbG()) + (float) addCarb[0]);

        if (scoredCount[0] > 0) {
            float existingScore = summary.getDayScore() != null ? summary.getDayScore() : 0f;
            float newAvgScore = (float) (addScoreSum[0] / scoredCount[0]);
            float mergedScore = (previousMeals + scoredCount[0]) > 0
                    ? (existingScore * previousMeals + newAvgScore * scoredCount[0]) / (previousMeals + scoredCount[0])
                    : newAvgScore;
            summary.setDayScore(mergedScore);
        }

        Integer snacks = summary.getTotalSnack() != null ? summary.getTotalSnack() : 0;
        Integer nights = summary.getTotalNight() != null ? summary.getTotalNight() : 0;
        if (mealTime != null) {
            switch (mealTime) {
                case SNACK -> summary.setTotalSnack(snacks + logs.size());
                case NIGHT -> summary.setTotalNight(nights + logs.size());
                default -> { /* no-op */ }
            }
        }

        summary.setUpdatedAt(now);
        // dayColor가 null이면 기본값 설정 (DB 제약 조건 준수)
        if (summary.getDayColor() == null) {
            summary.setDayColor("green");
        }
        DailyIntakeSummary savedSummary = dailyIntakeSummaryRepository.save(summary);

        logs.forEach(log -> log.setDailyIntakeSummary(savedSummary));
        mealLogRepository.saveAll(logs);

        log.info("[NutritionService] saved {} meal logs for user={} date={} time={}", logs.size(), user.getId(), mealDate, mealTime);
    }


    /**
     * 각 식사 후보 메뉴에 최소/최대/기본 섭취 비율을 세팅한다.
     */
    private void applyDefaultIntakeRanges(OrderImageMealLogResponse response) {
        if (response == null || response.getItems() == null) {
            return;
        }
        for (MealLogCandidateResponse item : response.getItems()) {
            if (item == null) continue;

            if (item.getIntakeMinRatio() == null) {
                item.setIntakeMinRatio(0.0f);
            }
            if (item.getIntakeMaxRatio() == null) {
                item.setIntakeMaxRatio(1.0f);
            }
            if (item.getIntakeDefaultRatio() == null) {
                item.setIntakeDefaultRatio(1.0f);
            }
        }
    }

    private User getCurrentUser() {
        return authenticatedUserProvider.getCurrentUser();
    }
}
