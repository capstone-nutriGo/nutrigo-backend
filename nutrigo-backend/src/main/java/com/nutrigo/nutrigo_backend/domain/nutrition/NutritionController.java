package com.nutrigo.nutrigo_backend.domain.nutrition;

import com.nutrigo.nutrigo_backend.domain.nutrition.dto.*;
import com.nutrigo.nutrigo_backend.global.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/nutrition")
@RequiredArgsConstructor
@Slf4j
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
        log.info("[NutritionController] cart-image 요청 수신: s3Key={}, captureId={}", 
                request.getS3Key(), request.getCaptureId());
        NutritionAnalysisResponse result = nutritionService.analyzeFromCartImage(request);
        log.info("[NutritionController] cart-image 응답 반환: analyses={}", 
                (result != null && result.getAnalyses() != null) ? result.getAnalyses().size() : 0);
        return ResponseEntity.ok(ApiResponse.success(result));
    }


    /**
     * POST /api/v1/nutrition/order-image
     * 주문내역 캡처 -> 식사 기록 후보 + 영양 정보
     */
    @PostMapping("/order-image")
    public ResponseEntity<ApiResponse<OrderImageMealLogResponse>> analyzeFromOrderImage(
            @RequestBody @Valid OrderImageAnalysisRequest request
    ) {
        OrderImageMealLogResponse response = nutritionService.analyzeFromOrderImage(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
