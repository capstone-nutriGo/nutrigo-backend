package com.nutrigo.nutrigo_backend.domain.nutrition;

import com.nutrigo.nutrigo_backend.domain.nutrition.dto.*;
import com.nutrigo.nutrigo_backend.global.infra.s3.S3Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class NutritionService {

    private final NutrigoAiClient nutrigoAiClient;
    private final S3Service s3Service;

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
     * 장바구니 캡처 이미지 기반 영양 분석
     */
    @Transactional(readOnly = true)
    public NutritionAnalysisResponse analyzeFromCartImage(CartImageAnalysisRequest request) {
        log.info("[NutritionService] cart-image 분석 시작: captureId={}, imageUrl={}, s3Key={}",
                request.getCaptureId(), request.getImageUrl(), request.getS3Key());

        // S3 키가 있으면 presigned GET URL로 변환하여 imageUrl에 설정
        CartImageAnalysisRequest processedRequest = processS3Key(request);

        NutritionAnalysisResponse response = nutrigoAiClient.analyzeCartImage(processedRequest);

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
    @Transactional(readOnly = true)
    public OrderImageMealLogResponse analyzeFromOrderImage(OrderImageAnalysisRequest request) {
        log.info("[NutritionService] order-image 분석 시작: captureId={}, imageUrl={}, s3Key={}, orderDate={}, mealTime={}",
                request.getCaptureId(), request.getImageUrl(), request.getS3Key(),
                request.getOrderDate(), request.getMealTime());

        // S3 키가 있으면 presigned GET URL로 변환하여 imageUrl에 설정
        OrderImageAnalysisRequest processedRequest = processS3Key(request);

        // 1) NutriGo-AI 호출 (Python 서버로 order-image 요청)
        OrderImageMealLogResponse response = nutrigoAiClient.analyzeOrderImage(processedRequest);

        // 2) 섭취량 슬라이더용 기본 범위 세팅
        applyDefaultIntakeRanges(response);

        // 3) 요청에서 받은 order_date / meal_time 을 응답 DTO에 그대로 실어주기
        if (response != null) {
            response.setOrderDate(request.getOrderDate());
            response.setMealTime(request.getMealTime());
        }

        log.info("[NutritionService] order-image 분석 완료: captureId={}, candidateCount={}",
                response != null ? response.getCaptureId() : null,
                (response != null && response.getItems() != null)
                        ? response.getItems().size()
                        : 0);

        return response;
    }

    /**
     * S3 키가 있으면 presigned GET URL로 변환하여 imageUrl에 설정
     */
    private CartImageAnalysisRequest processS3Key(CartImageAnalysisRequest request) {
        if (StringUtils.hasText(request.getS3Key()) && !StringUtils.hasText(request.getImageUrl())) {
            // S3 키를 presigned GET URL로 변환 (1시간 유효)
            String presignedUrl = s3Service.generatePresignedGetUrl(request.getS3Key(), 3600);
            request.setImageUrl(presignedUrl);
            log.info("[NutritionService] S3 키를 presigned URL로 변환: s3Key={}", request.getS3Key());
        }
        return request;
    }

    /**
     * S3 키가 있으면 presigned GET URL로 변환하여 imageUrl에 설정
     */
    private OrderImageAnalysisRequest processS3Key(OrderImageAnalysisRequest request) {
        if (StringUtils.hasText(request.getS3Key()) && !StringUtils.hasText(request.getImageUrl())) {
            // S3 키를 presigned GET URL로 변환 (1시간 유효)
            String presignedUrl = s3Service.generatePresignedGetUrl(request.getS3Key(), 3600);
            request.setImageUrl(presignedUrl);
            log.info("[NutritionService] S3 키를 presigned URL로 변환: s3Key={}", request.getS3Key());
        }
        return request;
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
}
