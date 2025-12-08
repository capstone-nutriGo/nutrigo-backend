package com.nutrigo.nutrigo_backend.domain.insight;

import com.nutrigo.nutrigo_backend.domain.insight.dto.InsightLogRequest;
import com.nutrigo.nutrigo_backend.domain.insight.dto.MealAnalysisRequest;
import com.nutrigo.nutrigo_backend.domain.insight.dto.MealAnalysisResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Slf4j
@Component
public class MealAnalysisClient {

    private final RestClient restClient;
    private final String analysisPath;

    public MealAnalysisClient(
            RestClient.Builder restClientBuilder,
            @Value("${internal.meal-analysis.base-url:http://localhost:8081}") String baseUrl,
            @Value("${internal.meal-analysis.path:/internal/meal-analysis}") String analysisPath
    ) {
        this.restClient = restClientBuilder.baseUrl(baseUrl).build();
        this.analysisPath = analysisPath;
    }

    public MealAnalysisResult analyze(InsightLogRequest request) {
        MealAnalysisRequest payload = new MealAnalysisRequest(
                request.menu(),
                request.foodImageUrl(),
                request.foodDescription(),
                request.serving(),
                request.mealtime(),
                request.mealDate()
        );

        try {
            MealAnalysisResult result = restClient.post()
                    .uri(analysisPath)
                    .body(payload)
                    .retrieve()
                    .body(MealAnalysisResult.class);

            log.debug("Meal analysis result for {}: {}", request.foodImageUrl(), result);
            return result;
        } catch (RestClientException e) {
            log.warn("Failed to analyze meal image {}: {}", request.foodImageUrl(), e.getMessage(), e);
            return null;
        }
    }
}