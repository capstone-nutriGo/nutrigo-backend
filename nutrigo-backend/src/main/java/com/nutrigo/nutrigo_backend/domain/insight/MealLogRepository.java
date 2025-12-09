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
}