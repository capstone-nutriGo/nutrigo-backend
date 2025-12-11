package com.nutrigo.nutrigo_backend.domain.insight;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.OffsetDateTime;
import com.nutrigo.nutrigo_backend.domain.insight.DailyIntakeSummary;

import java.time.LocalDate;
import java.util.List;

public interface MealLogRepository extends JpaRepository<MealLog, Long> {

    List<MealLog> findAllByMealDateBetween(LocalDate start, LocalDate end);

    List<MealLog> findAllByMealDate(LocalDate date);

    long countByDailyIntakeSummary(DailyIntakeSummary summary);

    // 유저별 필터링 메서드 추가
    List<MealLog> findAllByDailyIntakeSummary_UserAndMealDateBetween(com.nutrigo.nutrigo_backend.domain.user.User user, LocalDate start, LocalDate end);

    List<MealLog> findAllByDailyIntakeSummary_UserAndMealDate(com.nutrigo.nutrigo_backend.domain.user.User user, LocalDate date);
}