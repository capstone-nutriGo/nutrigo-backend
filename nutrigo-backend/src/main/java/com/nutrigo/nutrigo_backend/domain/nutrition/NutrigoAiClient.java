package com.nutrigo.nutrigo_backend.domain.nutrition;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nutrigo.nutrigo_backend.domain.nutrition.dto.CartImageAnalysisRequest;
import com.nutrigo.nutrigo_backend.domain.nutrition.dto.NutritionAnalysisResponse;
import com.nutrigo.nutrigo_backend.domain.nutrition.dto.OrderImageMealLogResponse;
import com.nutrigo.nutrigo_backend.domain.nutrition.dto.StoreLinkAnalysisRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import com.nutrigo.nutrigo_backend.domain.nutrition.dto.NutritionAnalysisResponse;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

@Slf4j
@Component
@RequiredArgsConstructor
public class NutrigoAiClient {

    private final ObjectMapper objectMapper;

    @Value("${nutrigo.ai.base-url}")
    private String nutrigoAiBaseUrl;   // 예: http://localhost:8000

    private final HttpClient httpClient = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_1_1)
            .build();

    /**
     * 공용 POST 호출 헬퍼
     * - bodyObj: DTO (StoreLinkAnalysisRequest, CartImageAnalysisRequest 등)
     *   -> 내부에서 JSON 문자열로 직렬화
     */
    private String postJson(String path, Object bodyObj) throws Exception {
        // 1) DTO -> JSON 문자열
        String jsonBody = objectMapper.writeValueAsString(bodyObj);
        byte[] bodyBytes = jsonBody.getBytes(StandardCharsets.UTF_8);

        String url = nutrigoAiBaseUrl + path;

        log.info("[NutriGo-AI] 요청 path={} JSON({} bytes) = {}", path, bodyBytes.length, jsonBody);

        // 2) HTTP 요청 생성 (Content-Type만 설정, Content-Length는 HttpClient가 알아서)
        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofByteArray(bodyBytes))
                .build();

        // 3) 전송 및 응답
        HttpResponse<String> response = httpClient.send(
                httpRequest,
                HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8)
        );

        int status = response.statusCode();
        String body = response.body();

        log.info("[NutriGo-AI] 응답 path={} status={}, body={}", path, status, body);

        if (status >= 400) {
            throw new RuntimeException("NutriGo-AI 호출 실패: path=" + path +
                    ", status=" + status + ", body=" + body);
        }

        return body;
    }

    /**
     * /internal/api/v1/nutrition/store-link
     */
    public NutritionAnalysisResponse analyzeStoreLink(StoreLinkAnalysisRequest request) {
        try {
            log.info("[NutriGo-AI] store-link 요청 DTO: storeUrl={}, focus={}",
                    request.getStoreUrl(),
                    request.getUserGoal() != null ? request.getUserGoal().getFocus() : null
            );

            String rawJson = postJson("/internal/api/v1/nutrition/store-link", request);
            // rawJson = {"analyses":[...], "summary":"...", "recommended_menu_ids":[...]}

            // JSON -> DTO
            return objectMapper.readValue(rawJson, NutritionAnalysisResponse.class);

        } catch (Exception e) {
            log.error("[NutriGo-AI] store-link 호출 중 오류", e);
            throw new RuntimeException("NutriGo-AI store-link 호출 실패", e);
        }
    }

    /**
     * /internal/api/v1/nutrition/cart-image
     * - 장바구니 이미지 분석 결과를 NutritionAnalysisResponse DTO로 반환
     */
    public NutritionAnalysisResponse analyzeCartImage(CartImageAnalysisRequest request) {
        try {
            log.info("[NutriGo-AI] cart-image 요청 DTO: {}", request);

            // FastAPI에서 내려주는 JSON 문자열
            String rawJson = postJson("/internal/api/v1/nutrition/cart-image", request);

            // JSON -> DTO 변환
            return objectMapper.readValue(rawJson, NutritionAnalysisResponse.class);

        } catch (Exception e) {
            log.error("[NutriGo-AI] cart-image 호출 중 오류", e);
            throw new RuntimeException("NutriGo-AI cart-image 호출 실패", e);
        }
    }


    /**
     * /internal/api/v1/nutrition/order-image
     * - 주문내역 이미지 분석 결과를 OrderImageMealLogResponse DTO로 변환
     */
    public OrderImageMealLogResponse analyzeOrderImage(CartImageAnalysisRequest request) {
        try {
            log.info("[NutriGo-AI] order-image 요청 DTO: {}", request);
            String responseBody = postJson("/internal/api/v1/nutrition/order-image", request);

            // 응답 JSON -> DTO
            return objectMapper.readValue(responseBody, OrderImageMealLogResponse.class);

        } catch (Exception e) {
            log.error("[NutriGo-AI] order-image 호출 중 오류", e);
            throw new RuntimeException("NutriGo-AI order-image 호출 실패", e);
        }
    }
}
