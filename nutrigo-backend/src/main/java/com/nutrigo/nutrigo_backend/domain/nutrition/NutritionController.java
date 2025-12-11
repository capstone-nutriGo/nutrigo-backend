package com.nutrigo.nutrigo_backend.domain.nutrition;

import com.nutrigo.nutrigo_backend.domain.nutrition.dto.CartImageAnalysisRequest;
import com.nutrigo.nutrigo_backend.domain.nutrition.dto.NutritionAnalysisResponse;
import com.nutrigo.nutrigo_backend.domain.nutrition.dto.OrderImageMealLogResponse;
import com.nutrigo.nutrigo_backend.domain.nutrition.dto.StoreLinkAnalysisRequest;
import com.nutrigo.nutrigo_backend.global.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/nutrition")
@RequiredArgsConstructor
public class NutritionController {

    private final NutritionService nutritionService;

    /**
     * POST /api/v1/nutrition/store-link
     * 배달앱 가게 링크 기반 영양 분석
     */
    @PostMapping("/store-link")
    public ResponseEntity<ApiResponse<NutritionAnalysisResponse>> analyzeFromStoreLink(
            @RequestBody @Valid StoreLinkAnalysisRequest request
    ) {
        NutritionAnalysisResponse result = nutritionService.analyzeFromStoreLink(request);
        return ResponseEntity.ok(ApiResponse.success(result));
    }


    /**
     * POST /api/v1/nutrition/cart-image
     * 장바구니 캡처 이미지 기반 영양 분석
     */
    @PostMapping("/cart-image")
    public ResponseEntity<ApiResponse<NutritionAnalysisResponse>> analyzeFromCartImage(
            @RequestBody @Valid CartImageAnalysisRequest request
    ) {
        NutritionAnalysisResponse result = nutritionService.analyzeFromCartImage(request);
        return ResponseEntity.ok(ApiResponse.success(result));
    }


    /**
     * POST /api/v1/nutrition/order-image
     * 주문내역 캡처 -> 식사 기록 후보 + 영양 정보
     */
    @PostMapping("/order-image")
    public ResponseEntity<ApiResponse<OrderImageMealLogResponse>> analyzeFromOrderImage(
            @RequestBody @Valid CartImageAnalysisRequest request
    ) {
        OrderImageMealLogResponse response = nutritionService.analyzeFromOrderImage(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
