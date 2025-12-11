package com.nutrigo.nutrigo_backend.domain.nutrition;

import com.nutrigo.nutrigo_backend.domain.nutrition.dto.CartImageAnalysisRequest;
import com.nutrigo.nutrigo_backend.domain.nutrition.dto.OrderImageMealLogResponse;
import com.nutrigo.nutrigo_backend.domain.nutrition.dto.StoreLinkAnalysisRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Slf4j
@Component
@RequiredArgsConstructor
public class NutrigoAiClient {

    private final RestClient nutrigoAiRestClient;

    public String analyzeStoreLink(StoreLinkAnalysisRequest request) {
        log.info("[NutriGo-AI] → store-link 요청: storeUrl={}, focus={}",
                request.getStoreUrl(),
                request.getUserGoal() != null ? request.getUserGoal().getFocus() : null
        );

        return nutrigoAiRestClient.post()
                .uri("/internal/api/v1/nutrition/store-link")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(String.class);
    }

    public String analyzeCartImage(CartImageAnalysisRequest request) {
        log.info("[NutriGo-AI] → cart-image 요청: imageUrl={}, captureId={}, focus={}",
                request.getImageUrl(),
                request.getCaptureId(),
                request.getUserGoal() != null ? request.getUserGoal().getFocus() : null
        );

        return nutrigoAiRestClient.post()
                .uri("/internal/api/v1/nutrition/cart-image")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(String.class);
    }

    public OrderImageMealLogResponse analyzeFromOrderImage(CartImageAnalysisRequest request) {
        log.info("[NutriGo-AI] → order-image 요청: imageUrl={}, captureId={}, focus={}",
                request.getImageUrl(),
                request.getCaptureId(),
                request.getUserGoal() != null ? request.getUserGoal().getFocus() : null
        );

        return nutrigoAiRestClient.post()
                .uri("/internal/api/v1/nutrition/order-image")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(OrderImageMealLogResponse.class);
    }
}
