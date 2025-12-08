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
import java.util.Map;
import java.util.Comparator;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InsightQueryService {

    private final DailyIntakeSummaryRepository dailyIntakeSummaryRepository;
    private final MealLogRepository mealLogRepository;

    public WeeklyInsightSummaryResponse getWeeklySummary(LocalDate baseDate) {
        LocalDate weekStart = baseDate.with(DayOfWeek.MONDAY);
        LocalDate weekEnd = baseDate.with(DayOfWeek.SUNDAY);

        List<DailyIntakeSummary> summaries = dailyIntakeSummaryRepository.findAllByDateBetween(weekStart, weekEnd);
        List<MealLog> mealLogs = mealLogRepository.findAllByMealDateBetween(weekStart, weekEnd);

        int totalMeals = mealLogs.size();

        long goodDays = summaries.stream().filter(summary -> "green".equalsIgnoreCase(summary.getDayColor())).count();
        long overeatDays = summaries.stream().filter(summary -> "red".equalsIgnoreCase(summary.getDayColor())).count();
        long lowSodiumDays = summaries.stream().filter(summary -> "yellow".equalsIgnoreCase(summary.getDayColor())).count();

        DoubleSummaryStatistics scoreStats = summaries.stream()
                .map(DailyIntakeSummary::getDayScore)
                .filter(score -> score != null)
                .mapToDouble(Float::doubleValue)
                .summaryStatistics();

        double totalKcal = summaries.stream()
                .map(DailyIntakeSummary::getTotalKcal)
                .filter(kcal -> kcal != null)
                .mapToDouble(Float::doubleValue)
                .sum();

        List<WeeklyInsightSummaryResponse.TrendDay> trendDays = summaries.stream()
                .sorted(Comparator.comparing(DailyIntakeSummary::getDate))
                .map(summary -> new WeeklyInsightSummaryResponse.TrendDay(
                        summary.getDate(),
                        summary.getDayScore(),
                        summary.getDayColor(),
                        summary.getTotalKcal(),
                        summary.getTotalCarbG(),
                        summary.getTotalProteinG(),
                        summary.getTotalSodiumMg()
                ))
                .toList();

        List<WeeklyInsightSummaryResponse.CategoryStat> categoryTop3 = mealLogs.stream()
                .map(MealLog::getCategory)
                .map(category -> (category == null || category.isBlank()) ? "UNCATEGORIZED" : category)
                .collect(Collectors.groupingBy(category -> category, Collectors.counting()))
                .entrySet()
                .stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(3)
                .map(entry -> new WeeklyInsightSummaryResponse.CategoryStat(entry.getKey(), entry.getValue()))
                .toList();

        WeeklyInsightSummaryResponse.Summary summary = new WeeklyInsightSummaryResponse.Summary(
                totalMeals,
                (int) goodDays,
                (int) overeatDays,
                (int) lowSodiumDays,
                scoreStats.getCount() > 0 ? scoreStats.getAverage() : 0.0,
                totalMeals > 0 ? totalKcal / totalMeals : 0.0
        );

        WeeklyInsightSummaryResponse.Trends trends = new WeeklyInsightSummaryResponse.Trends(trendDays);

        WeeklyInsightSummaryResponse.Data data = new WeeklyInsightSummaryResponse.Data(
                weekStart,
                weekEnd,
                summary,
                trends,
                categoryTop3
        );

        return new WeeklyInsightSummaryResponse(true, data);
    }

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
                        mealLog.getMenu(),
                        mealLog.getCategory(),
                        mealLog.getMealTime(),
                        mealLog.getMealDate(),
                        mealLog.getCreatedAt(),
                        mealLog.getKcal(),
                        mealLog.getSodiumMg(),
                        mealLog.getProteinG(),
                        mealLog.getCarbG(),
                        mealLog.getTotalScore()
                ))
                .toList();

        DayMealsResponse.Data data = new DayMealsResponse.Data(
                date,
                summary != null ? summary.getTotalKcal() : null,
                summary != null ? summary.getTotalSodiumMg() : null,
                summary != null ? summary.getTotalProteinG() : null,
                summary != null ? summary.getTotalCarbG() : null,
                meals.size(),
                summary != null ? summary.getTotalSnack() : null,
                summary != null ? summary.getTotalNight() : null,
                summary != null ? summary.getDayScore() : null,
                summary != null ? summary.getDayColor() : null,
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