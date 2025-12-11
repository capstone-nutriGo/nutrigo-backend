package com.nutrigo.nutrigo_backend.domain.insight;

import com.nutrigo.nutrigo_backend.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface MealLogRepository extends JpaRepository<MealLog, Long> {

    List<MealLog> findAllByMealDateBetween(LocalDate start, LocalDate end);

    List<MealLog> findAllByDailyIntakeSummary_UserAndMealDateBetween(User user, LocalDate start, LocalDate end);

    List<MealLog> findAllByMealDate(LocalDate date);

    List<MealLog> findAllByDailyIntakeSummary_UserAndMealDate(User user, LocalDate date);

    long countByDailyIntakeSummary(DailyIntakeSummary summary);
}