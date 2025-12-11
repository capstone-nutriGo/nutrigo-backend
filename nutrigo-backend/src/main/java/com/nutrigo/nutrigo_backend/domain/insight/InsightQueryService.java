package com.nutrigo.nutrigo_backend.domain.insight;

import com.nutrigo.nutrigo_backend.domain.insight.dto.*;
import com.nutrigo.nutrigo_backend.domain.user.User;
import com.nutrigo.nutrigo_backend.domain.user.UserService;
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
    private final UserService userService;

    public WeeklyInsightSummaryResponse getWeeklySummary(LocalDate baseDate, String authorization) {
        User user = userService.getCurrentUser(authorization);
        LocalDate weekStart = baseDate.with(DayOfWeek.MONDAY);
        LocalDate weekEnd = baseDate.with(DayOfWeek.SUNDAY);

        List<DailyIntakeSummary> summaries = dailyIntakeSummaryRepository.findAllByUserAndDateBetween(user, weekStart, weekEnd);
        List<MealLog> mealLogs = mealLogRepository.findAllByDailyIntakeSummary_UserAndMealDateBetween(user, weekStart, weekEnd);

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
                .map(mealLog -> {
                    String category = mealLog.getCategory();
                    // 카테고리가 없으면 메뉴명에서 추론
                    if (category == null || category.isBlank()) {
                        category = inferCategoryFromMenu(mealLog.getMenu());
                    }
                    return category;
                })
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

    public InsightReportResponse getReport(String range, LocalDate baseDate, String authorization) {
        User user = userService.getCurrentUser(authorization);
        InsightReportResponse.ReportRange reportRange = parseRange(range);
        LocalDate startDate = calculateStartDate(reportRange, baseDate);
        LocalDate endDate = calculateEndDate(reportRange, baseDate);

        List<DailyIntakeSummary> summaries = dailyIntakeSummaryRepository.findAllByUserAndDateBetween(user, startDate, endDate);

        int totalMeals = mealLogRepository.findAllByDailyIntakeSummary_UserAndMealDateBetween(user, startDate, endDate).size();

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

    public InsightCalendarResponse getCalendar(LocalDate startDate, LocalDate endDate, String authorization) {
        User user = userService.getCurrentUser(authorization);
        List<DailyIntakeSummary> summaries = dailyIntakeSummaryRepository.findAllByUserAndDateBetween(user, startDate, endDate);

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

    public DayMealsResponse getDayMeals(LocalDate date, String authorization) {
        User user = userService.getCurrentUser(authorization);
        DailyIntakeSummary summary = dailyIntakeSummaryRepository.findByUserAndDate(user, date).orElse(null);

        List<MealLog> mealLogs = mealLogRepository.findAllByDailyIntakeSummary_UserAndMealDate(user, date);
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

    /**
     * 메뉴명에서 카테고리를 추론하는 유틸리티 메서드
     */
    private String inferCategoryFromMenu(String menu) {
        if (menu == null || menu.isBlank()) {
            return "UNCATEGORIZED";
        }
        
        String menuLower = menu.toLowerCase();
        
        // 일식
        if (menuLower.contains("초밥") || menuLower.contains("회") || menuLower.contains("연어") 
            || menuLower.contains("담다") || menuLower.contains("사시미") || menuLower.contains("우동")
            || menuLower.contains("라멘") || menuLower.contains("돈부리") || menuLower.contains("가츠")
            || menuLower.contains("돈까스") || menuLower.contains("규동") || menuLower.contains("오니기리")) {
            return "일식";
        }
        
        // 중식
        if (menuLower.contains("짜장") || menuLower.contains("짬뽕") || menuLower.contains("볶음밥")
            || menuLower.contains("탕수육") || menuLower.contains("마파두부") || menuLower.contains("양장피")
            || menuLower.contains("깐풍") || menuLower.contains("유산슬") || menuLower.contains("팔보채")
            || menuLower.contains("마라") || menuLower.contains("훠궈") || menuLower.contains("딤섬")) {
            return "중식";
        }
        
        // 한식
        if (menuLower.contains("비빔밥") || menuLower.contains("한우") || menuLower.contains("생육회")
            || menuLower.contains("김치") || menuLower.contains("된장") || menuLower.contains("국밥")
            || menuLower.contains("냉면") || menuLower.contains("불고기") || menuLower.contains("갈비")
            || menuLower.contains("삼겹살") || menuLower.contains("보쌈") || menuLower.contains("족발")
            || menuLower.contains("떡볶이") || menuLower.contains("순두부") || menuLower.contains("부대찌개")
            || menuLower.contains("김밥") || menuLower.contains("라면") || menuLower.contains("국수")) {
            return "한식";
        }
        
        // 치킨
        if (menuLower.contains("치킨") || menuLower.contains("닭") || menuLower.contains("윙")
            || menuLower.contains("닭강정") || menuLower.contains("후라이드") || menuLower.contains("양념")) {
            return "치킨";
        }
        
        // 양식
        if (menuLower.contains("파스타") || menuLower.contains("스테이크") || menuLower.contains("피자")
            || menuLower.contains("햄버거") || menuLower.contains("샐러드") || menuLower.contains("리조또")
            || menuLower.contains("스파게티") || menuLower.contains("라자냐") || menuLower.contains("그라탕")) {
            return "양식";
        }
        
        // 분식
        if (menuLower.contains("떡볶이") || menuLower.contains("순대") || menuLower.contains("튀김")
            || menuLower.contains("어묵") || menuLower.contains("핫도그") || menuLower.contains("만두")) {
            return "분식";
        }
        
        // 카페/디저트
        if (menuLower.contains("케이크") || menuLower.contains("마카롱") || menuLower.contains("도넛")
            || menuLower.contains("와플") || menuLower.contains("크로플") || menuLower.contains("빙수")) {
            return "디저트";
        }
        
        return "UNCATEGORIZED";
    }
}