package com.nutrigo.nutrigo_backend.domain.nutrition;

import com.nutrigo.nutrigo_backend.domain.nutrition.dto.CartImageAnalysisRequest;
import com.nutrigo.nutrigo_backend.domain.nutrition.dto.NutritionAnalysisResponse;
import com.nutrigo.nutrigo_backend.domain.nutrition.dto.OrderImageMealLogResponse;
import com.nutrigo.nutrigo_backend.domain.nutrition.dto.StoreLinkAnalysisRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NutritionService {

    private final NutrigoAiClient nutrigoAiClient;

    public NutritionAnalysisResponse analyzeFromStoreLink(StoreLinkAnalysisRequest request) {
        return nutrigoAiClient.analyzeStoreLink(request);
    }

    public NutritionAnalysisResponse analyzeFromCartImage(CartImageAnalysisRequest request) {
        return nutrigoAiClient.analyzeCartImage(request);
    }

    public OrderImageMealLogResponse analyzeFromOrderImage(CartImageAnalysisRequest request) {
        return nutrigoAiClient.analyzeOrderImage(request);
    }
}
