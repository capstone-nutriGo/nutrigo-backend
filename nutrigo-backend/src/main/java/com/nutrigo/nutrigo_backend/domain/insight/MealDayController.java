package com.nutrigo.nutrigo_backend.domain.insight;

import com.nutrigo.nutrigo_backend.domain.insight.dto.DayMealsResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/meals")
@RequiredArgsConstructor
public class MealDayController {

    private final InsightQueryService insightQueryService;

    @GetMapping("/day")
    public ResponseEntity<DayMealsResponse> getDayMeals(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        DayMealsResponse response = insightQueryService.getDayMeals(date);
        return ResponseEntity.ok(response);
    }
}
