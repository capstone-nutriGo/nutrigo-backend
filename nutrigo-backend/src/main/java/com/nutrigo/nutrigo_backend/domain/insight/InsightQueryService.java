package com.nutrigo.nutrigo_backend.domain.insight;

import com.nutrigo.nutrigo_backend.domain.insight.dto.*;
import com.nutrigo.nutrigo_backend.global.error.AppExceptions.Insight.InvalidReportRangeException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.*;
import java.util.ArrayList;
import java.util.DoubleSummaryStatistics;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class InsightQueryService {

    private final DailyIntakeSummaryRepository dailyIntakeSummaryRepository;
    private final MealLogRepository mealLogRepository;

    public InsightReportResponse getReport(String range, LocalDate baseDate) {
        InsightReportResponse.ReportRange reportRange = parseRange(range);
        LocalDate startDate = calculateStartDate(reportRange, baseDate);
        LocalDate endDate = calculateEndDate(reportRange, baseDate);

        List<DailyIntakeSummary> summaries = dailyIntakeSummaryRepository.findAllByDateBetween(startDate, endDate);

        int totalMeals = mealLogRepository.findAllByMealDateBetween(startDate, endDate).size();

        long goodDays = summaries.stream().filter(summary -> "green".equalsIgnoreCase(summary.getDayColor())).count();
        long overeatDays = summaries.stream().filter(summary -> "red".equalsIgnoreCase(summary.getDayColor())).count();
        long lowSodiumDays = summaries.stream().filter(summary -> "yellow".equalsIgnoreCase(summary.getDayColor())).count();

        DoubleSummaryStatistics scoreStats = summaries.stream()
                .map(DailyIntakeSummary::getDayScore)
                .filter(score -> score != null)
                .mapToDouble(Float::doubleValue)
                .summaryStatistics();

        double avgScore = scoreStats.getCount() > 0 ? scoreStats.getAverage() : 0.0;

        InsightReportResponse.Summary summary = new InsightReportResponse.Summary(
                totalMeals,
                (int) goodDays,
                (int) overeatDays,
                (int) lowSodiumDays,
                avgScore
        );

        InsightReportResponse.Patterns patterns = new InsightReportResponse.Patterns(
                new InsightReportResponse.LateSnack(0)
        );

        InsightReportResponse.Data data = new InsightReportResponse.Data(
                reportRange,
                startDate,
                endDate,
                summary,
                patterns
        );

        return new InsightReportResponse(true, data);
    }

    public InsightCalendarResponse getCalendar(LocalDate startDate, LocalDate endDate) {
        List<DailyIntakeSummary> summaries = dailyIntakeSummaryRepository.findAllByDateBetween(startDate, endDate);

        List<InsightCalendarResponse.Day> days = summaries.stream()
                .map(this::toCalendarDay)
                .toList();

        InsightCalendarResponse.Data data = new InsightCalendarResponse.Data(
                startDate,
                endDate,
                days
        );

        return new InsightCalendarResponse(true, data);
    }

    public DayMealsResponse getDayMeals(LocalDate date) {
        DailyIntakeSummary summary = dailyIntakeSummaryRepository.findByDate(date).orElse(null);

        List<MealLog> mealLogs = mealLogRepository.findAllByMealDate(date);
        List<DayMealsResponse.Meal> meals = mealLogs.stream()
                .map(mealLog -> new DayMealsResponse.Meal(
                        mealLog.getId(),
                        mealLog.getMealTime(),
                        mealLog.getMealDate(),
                        mealLog.getCreatedAt()
                ))
                .toList();

        DayMealsResponse.Data data = new DayMealsResponse.Data(
                date,
                summary != null ? summary.getTotalKcal() : null,
                summary != null ? summary.getTotalSodiumMg() : null,
                summary != null ? summary.getTotalProteinG() : null,
                meals.size(),
                meals
        );

        return new DayMealsResponse(true, data);
    }

    private LocalDate calculateStartDate(InsightReportResponse.ReportRange range, LocalDate baseDate) {
        return switch (range) {
            case WEEKLY -> baseDate.with(DayOfWeek.MONDAY);
            case MONTHLY -> baseDate.withDayOfMonth(1);
        };
    }

    private LocalDate calculateEndDate(InsightReportResponse.ReportRange range, LocalDate baseDate) {
        return switch (range) {
            case WEEKLY -> baseDate.with(DayOfWeek.SUNDAY);
            case MONTHLY -> baseDate.withDayOfMonth(baseDate.lengthOfMonth());
        };
    }

    private InsightCalendarResponse.Day toCalendarDay(DailyIntakeSummary summary) {
        List<String> tags = new ArrayList<>();
        if ("red".equalsIgnoreCase(summary.getDayColor())) {
            tags.add("과식 폭주");
        } else if ("yellow".equalsIgnoreCase(summary.getDayColor())) {
            tags.add("주의 필요");
        } else if ("green".equalsIgnoreCase(summary.getDayColor())) {
            tags.add("좋은 습관 유지");
        }

        DayHighlight highlight = DayHighlight.NEUTRAL;
        if ("red".equalsIgnoreCase(summary.getDayColor())) {
            highlight = DayHighlight.BAD;
        } else if ("green".equalsIgnoreCase(summary.getDayColor())) {
            highlight = DayHighlight.GOOD;
        }

        return new InsightCalendarResponse.Day(
                summary.getDate(),
                tags,
                summary.getDayScore(),
                highlight
        );
    }

    private InsightReportResponse.ReportRange parseRange(String value) {
        try {
            return InsightReportResponse.ReportRange.valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new InvalidReportRangeException(value);
        }
    }
}