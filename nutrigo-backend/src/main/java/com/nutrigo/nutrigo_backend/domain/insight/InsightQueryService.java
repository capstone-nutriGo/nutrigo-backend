package com.nutrigo.nutrigo_backend.domain.insight;

import com.nutrigo.nutrigo_backend.domain.insight.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.*;
import java.util.ArrayList;
import java.util.DoubleSummaryStatistics;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

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

        int totalMeals = summaries.stream()
                .map(DailyIntakeSummary::getTotalMeals)
                .filter(value -> value != null && value > 0)
                .mapToInt(Integer::intValue)
                .sum();

        long goodDays = summaries.stream().filter(summary -> Boolean.TRUE.equals(summary.getGoodDay())).count();
        long overeatDays = summaries.stream().filter(summary -> Boolean.TRUE.equals(summary.getOvereatDay())).count();
        long lowSodiumDays = summaries.stream().filter(summary -> Boolean.TRUE.equals(summary.getLowSodiumDay())).count();

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
                .collect(Collectors.toList());

        InsightCalendarResponse.Data data = new InsightCalendarResponse.Data(
                startDate,
                endDate,
                days
        );

        return new InsightCalendarResponse(true, data);
    }

    public DayMealsResponse getDayMeals(LocalDate date) {
        DailyIntakeSummary summary = dailyIntakeSummaryRepository.findByDate(date).orElse(null);

        OffsetDateTime start = date.atStartOfDay().atOffset(ZoneOffset.UTC);
        OffsetDateTime end = date.plusDays(1).atStartOfDay().atOffset(ZoneOffset.UTC).minusNanos(1);

        List<MealLog> mealLogs = mealLogRepository.findAllByOrderedAtBetween(start, end);
        List<DayMealsResponse.Meal> meals = mealLogs.stream()
                .map(mealLog -> new DayMealsResponse.Meal(
                        mealLog.getId(),
                        mealLog.getSource(),
                        mealLog.getMealTime(),
                        mealLog.getOrderedAt()
                ))
                .collect(Collectors.toList());

        DayMealsResponse.Data data = new DayMealsResponse.Data(
                date,
                summary != null ? summary.getTotalKcal() : null,
                summary != null ? summary.getTotalSodiumMg() : null,
                summary != null ? summary.getTotalProteinG() : null,
                summary != null ? summary.getTotalMeals() : meals.size(),
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
        if (Boolean.TRUE.equals(summary.getLowSodiumDay())) {
            tags.add("저염 성공");
        }
        if (Boolean.TRUE.equals(summary.getOvereatDay())) {
            tags.add("과식 폭주");
        }
        if (Boolean.TRUE.equals(summary.getGoodDay())) {
            tags.add("좋은 습관 유지");
        }

        DayHighlight highlight = DayHighlight.NEUTRAL;
        if (Boolean.TRUE.equals(summary.getOvereatDay())) {
            highlight = DayHighlight.BAD;
        } else if (Boolean.TRUE.equals(summary.getGoodDay()) || Boolean.TRUE.equals(summary.getLowSodiumDay())) {
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
        return InsightReportResponse.ReportRange.valueOf(value.trim().toUpperCase(Locale.ROOT));
    }
}