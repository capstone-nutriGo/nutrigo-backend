package com.nutrigo.nutrigo_backend.domain.nutrition;

import com.nutrigo.nutrigo_backend.domain.nutrition.dto.*;
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
     * POST /api/v1/nutrition/upload-url
     * 이미지 업로드용 presigned PUT URL 발급
     */
    @PostMapping("/upload-url")
    public ResponseEntity<ApiResponse<ImageUploadUrlResponse>> createUploadUrl(
            @RequestBody @Valid ImageUploadUrlRequest request
    ) {
        ImageUploadUrlResponse result = nutritionService.createUploadUrl(request);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    /**
     * POST /api/v1/nutrition/cart-image/upload-url
     * CART 이미지 전용 presigned PUT URL 발급
     */
    @PostMapping("/cart-image/upload-url")
    public ResponseEntity<ApiResponse<ImageUploadUrlResponse>> createCartUploadUrl(
            @RequestBody @Valid ImageUploadContentTypeRequest request
    ) {
        ImageUploadUrlResponse result = nutritionService.createCartImageUploadUrl(request);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    /**
     * POST /api/v1/nutrition/order-image/upload-url
     * ORDER 이미지 전용 presigned PUT URL 발급
     */
    @PostMapping("/order-image/upload-url")
    public ResponseEntity<ApiResponse<ImageUploadUrlResponse>> createOrderUploadUrl(
            @RequestBody @Valid ImageUploadContentTypeRequest request
    ) {
        ImageUploadUrlResponse result = nutritionService.createOrderImageUploadUrl(request);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

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
            @RequestBody @Valid OrderImageAnalysisRequest request
    ) {
        OrderImageMealLogResponse response = nutritionService.analyzeFromOrderImage(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
