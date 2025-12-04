package com.nutrigo.nutrigo_backend.domain.insight;

import com.nutrigo.nutrigo_backend.domain.insight.dto.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/insights")
@RequiredArgsConstructor
public class InsightLogController {

    private final InsightLogService insightLogService;
    private final InsightQueryService insightQueryService;

    @PostMapping("/logs")
    /*
        #swagger.summary = '인사이트 기록 생성'
        #swagger.description = '식사 분석 결과를 기반으로 인사이트 로그를 저장하고 관련 챌린지를 갱신합니다.'
        #swagger.requestBody = {
          required: true,
          content: {
            "application/json": {
              schema: {
                type: "object",
                properties: {
                  source: { type: "string", description: "데이터 출처 (예: OCR, USER)" },
                  analysisId: { type: "number", description: "연결된 분석 ID" },
                  mealtime: { type: "string", description: "식사 시간대 (BREAKFAST/LUNCH/DINNER/SNACK)" },
                  orderedAt: { type: "string", format: "date-time", description: "주문/섭취 시각 (ISO 8601)" }
                },
                required: ["source", "analysisId", "mealtime", "orderedAt"]
              }
            }
          }
        }
    */
    public ResponseEntity<InsightLogResponse> logInsight(@Valid @RequestBody InsightLogRequest request) {
        InsightLogResponse response = insightLogService.logInsight(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/reports")
    /*
        #swagger.summary = '인사이트 리포트 조회'
        #swagger.description = '기간(range)과 기준일(baseDate)에 맞춰 식습관 리포트를 조회합니다.'
        #swagger.parameters['range'] = {
          in: 'query',
          required: true,
          description: '리포트 범위 (WEEKLY/MONTHLY)',
          type: 'string'
        }
        #swagger.parameters['baseDate'] = {
          in: 'query',
          required: true,
          description: '리포트 기준 날짜 (YYYY-MM-DD)',
          type: 'string'
        }
    */
    public ResponseEntity<InsightReportResponse> getReport(
            @RequestParam String range,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate baseDate
    ) {
        InsightReportResponse response = insightQueryService.getReport(range, baseDate);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/calendar")
    /*
        #swagger.summary = '식사 캘린더 조회'
        #swagger.description = '특정 기간 동안의 식사 기록 캘린더 데이터를 반환합니다.'
        #swagger.parameters['startDate'] = {
          in: 'query',
          required: true,
          description: '조회 시작일 (YYYY-MM-DD)',
          type: 'string'
        }
        #swagger.parameters['endDate'] = {
          in: 'query',
          required: true,
          description: '조회 종료일 (YYYY-MM-DD)',
          type: 'string'
        }
    */
    public ResponseEntity<InsightCalendarResponse> getCalendar(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        InsightCalendarResponse response = insightQueryService.getCalendar(startDate, endDate);
        return ResponseEntity.ok(response);
    }
}