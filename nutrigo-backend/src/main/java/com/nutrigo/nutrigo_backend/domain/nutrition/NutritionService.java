package com.nutrigo.nutrigo_backend.domain.nutrition;

import com.nutrigo.nutrigo_backend.domain.nutrition.dto.CartImageAnalysisRequest;
import com.nutrigo.nutrigo_backend.domain.nutrition.dto.OrderImageMealLogResponse;
import com.nutrigo.nutrigo_backend.domain.nutrition.dto.StoreLinkAnalysisRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NutritionService {

    private final NutrigoAiClient nutrigoAiClient;

    public String analyzeFromStoreLink(StoreLinkAnalysisRequest request) {
        return nutrigoAiClient.analyzeStoreLink(request);
    }

    public String analyzeFromCartImage(CartImageAnalysisRequest request) {
        return nutrigoAiClient.analyzeCartImage(request);
    }

    public OrderImageMealLogResponse analyzeFromOrderImage(CartImageAnalysisRequest request) {
        OrderImageMealLogResponse response = nutrigoAiClient.analyzeOrderImage(request);

        // TODO: 여기서 response.items 를 MealLog 엔티티로 변환 후 저장 로직 추가
        //  ex) mealLogRepository.saveAll(...)

        return response;
    }
}
