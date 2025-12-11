package com.nutrigo.nutrigo_backend.domain.nutribot;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nutrigo.nutrigo_backend.domain.insight.DailyIntakeSummary;
import com.nutrigo.nutrigo_backend.domain.insight.DailyIntakeSummaryRepository;
import com.nutrigo.nutrigo_backend.domain.insight.MealLog;
import com.nutrigo.nutrigo_backend.domain.insight.MealLogRepository;
import com.nutrigo.nutrigo_backend.domain.nutribot.dto.NutriBotChatRequest;
import com.nutrigo.nutrigo_backend.domain.nutribot.dto.NutriBotChatResponse;
import com.nutrigo.nutrigo_backend.domain.user.User;
import com.nutrigo.nutrigo_backend.global.security.AuthenticatedUserProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class NutriBotService {

    private final ObjectMapper objectMapper;
    private final AuthenticatedUserProvider authenticatedUserProvider;
    private final DailyIntakeSummaryRepository dailyIntakeSummaryRepository;
    private final MealLogRepository mealLogRepository;

    @Value("${nutrigo.ai.base-url}")
    private String nutrigoAiBaseUrl;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_1_1)
            .build();

    @Transactional(readOnly = true)
    public NutriBotChatResponse chat(NutriBotChatRequest request) {
        User user = getCurrentUser();
        log.info("[NutriBotService] chat 요청 - userId: {}, message: {}", user.getId(), request.getMessage());

        try {
            // 사용자 데이터 수집
            Map<String, Object> aiRequest = buildAiRequest(user, request.getMessage());

            // AI 서비스 호출
            String responseBody = callAiService(aiRequest);
            
            // 응답 파싱
            @SuppressWarnings("unchecked")
            Map<String, Object> responseMap = objectMapper.readValue(responseBody, Map.class);
            
            @SuppressWarnings("unchecked")
            List<String> recommendedActions = responseMap.get("recommended_actions") != null 
                    ? (List<String>) responseMap.get("recommended_actions")
                    : List.of();
            
            return NutriBotChatResponse.builder()
                    .reply((String) responseMap.get("reply"))
                    .tone((String) responseMap.get("tone"))
                    .recommendedActions(recommendedActions)
                    .build();

        } catch (Exception e) {
            log.error("[NutriBotService] AI 서비스 호출 실패", e);
            throw new RuntimeException("챗봇 응답 생성 실패: " + e.getMessage(), e);
        }
    }

    private Map<String, Object> buildAiRequest(User user, String userMessage) {
        LocalDate today = LocalDate.now();
        LocalDate weekAgo = today.minusDays(7);

        // 최근 7일간의 daily summaries
        List<DailyIntakeSummary> summaries = dailyIntakeSummaryRepository
                .findAllByUserAndDateBetween(user, weekAgo, today);

        // 최근 meal logs
        List<MealLog> recentMeals = mealLogRepository
                .findAllByDailyIntakeSummary_UserAndMealDateBetween(user, weekAgo, today);

        // UserGoal 생성 (기본값 사용)
        Map<String, Object> userGoal = new HashMap<>();
        userGoal.put("focus", "maintenance"); // 기본값
        userGoal.put("calorie_min", null);
        userGoal.put("calorie_max", null);
        userGoal.put("protein_min", null);
        userGoal.put("fat_max", null);
        userGoal.put("carb_max", null);
        userGoal.put("sodium_max", null);

        // DailySummary 리스트
        List<Map<String, Object>> dailySummaries = summaries.stream()
                .map(summary -> {
                    Map<String, Object> ds = new HashMap<>();
                    ds.put("date", summary.getDate().toString());
                    ds.put("total_kcal", summary.getTotalKcal() != null ? summary.getTotalKcal() : 0.0);
                    ds.put("total_sodium_mg", summary.getTotalSodiumMg() != null ? summary.getTotalSodiumMg() : 0.0);
                    ds.put("total_protein_g", summary.getTotalProteinG() != null ? summary.getTotalProteinG() : 0.0);
                    ds.put("total_carb_g", summary.getTotalCarbG() != null ? summary.getTotalCarbG() : 0.0);
                    ds.put("total_fat_g", 0.0); // DailyIntakeSummary에 fat 필드가 없으므로 0.0으로 설정
                    return ds;
                })
                .collect(Collectors.toList());

        // RecentMenus 리스트
        List<Map<String, Object>> recentMenus = recentMeals.stream()
                .limit(10) // 최근 10개만
                .map(meal -> {
                    Map<String, Object> menu = new HashMap<>();
                    Map<String, Object> menuText = new HashMap<>();
                    menuText.put("id", meal.getId().toString());
                    menuText.put("name", meal.getMenu() != null ? meal.getMenu() : "알 수 없음");
                    menuText.put("description", "");
                    menuText.put("price", null);
                    menuText.put("category_hint", meal.getCategory());
                    menuText.put("option_text", null);

                    Map<String, Object> nutrition = new HashMap<>();
                    nutrition.put("kcal", meal.getKcal() != null ? meal.getKcal() : 0.0);
                    nutrition.put("carb_g", meal.getCarbG() != null ? meal.getCarbG() : 0.0);
                    nutrition.put("protein_g", meal.getProteinG() != null ? meal.getProteinG() : 0.0);
                    nutrition.put("fat_g", 0.0); // MealLog에 fat 필드가 없으므로 0.0으로 설정
                    nutrition.put("sodium_mg", meal.getSodiumMg() != null ? meal.getSodiumMg() : 0.0);
                    nutrition.put("confidence", 0.8f);

                    menu.put("menu", menuText);
                    menu.put("nutrition", nutrition);
                    menu.put("score", meal.getTotalScore() != null ? meal.getTotalScore() : 50.0f);
                    menu.put("badges", List.of());
                    menu.put("coach_sentence", "");

                    return menu;
                })
                .collect(Collectors.toList());

        Map<String, Object> request = new HashMap<>();
        request.put("mode", "chat");
        request.put("user_goal", userGoal);
        request.put("daily_summaries", dailySummaries);
        request.put("recent_menus", recentMenus);
        request.put("user_message", userMessage);

        return request;
    }

    private String callAiService(Map<String, Object> requestBody) throws Exception {
        String jsonBody = objectMapper.writeValueAsString(requestBody);
        byte[] bodyBytes = jsonBody.getBytes(StandardCharsets.UTF_8);

        String url = nutrigoAiBaseUrl + "/internal/api/v1/nutribot/coach";
        log.info("[NutriBotService] AI 서비스 호출 - url: {}, body: {}", url, jsonBody);

        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofByteArray(bodyBytes))
                .build();

        HttpResponse<String> response = httpClient.send(
                httpRequest,
                HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8)
        );

        int status = response.statusCode();
        String body = response.body();

        log.info("[NutriBotService] AI 서비스 응답 - status: {}, body: {}", status, body);

        if (status >= 400) {
            throw new RuntimeException("NutriBot AI 서비스 호출 실패: status=" + status + ", body=" + body);
        }

        return body;
    }

    private User getCurrentUser() {
        return authenticatedUserProvider.getCurrentUser();
    }
}

