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
import org.springframework.web.bind.annotation.RequestHeader;
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
    public ResponseEntity<InsightLogResponse> logInsight(
            @Valid @RequestBody InsightLogRequest request,
            @RequestHeader(value = "Authorization", required = false) String authorization
    ) {
        InsightLogResponse response = insightLogService.logInsight(request, authorization);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/reports")
    public ResponseEntity<InsightReportResponse> getReport(
            @RequestParam String range,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate baseDate,
            @RequestHeader(value = "Authorization", required = false) String authorization
    ) {
        InsightReportResponse response = insightQueryService.getReport(range, baseDate, authorization);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/calendar")
    public ResponseEntity<InsightCalendarResponse> getCalendar(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestHeader(value = "Authorization", required = false) String authorization
    ) {
        InsightCalendarResponse response = insightQueryService.getCalendar(startDate, endDate, authorization);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/weekly-summary")
    public ResponseEntity<WeeklyInsightSummaryResponse> getWeeklySummary(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate baseDate,
            @RequestHeader(value = "Authorization", required = false) String authorization
    ) {
        WeeklyInsightSummaryResponse response = insightQueryService.getWeeklySummary(baseDate, authorization);
        return ResponseEntity.ok(response);
    }
}