package com.nutrigo.nutrigo_backend.domain.insight;

import com.nutrigo.nutrigo_backend.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface MealLogRepository extends JpaRepository<MealLog, Long> {

    List<MealLog> findAllByMealDateBetween(LocalDate start, LocalDate end);

    @Query("SELECT ml FROM MealLog ml JOIN ml.dailyIntakeSummary dis WHERE dis.user = :user AND ml.mealDate BETWEEN :start AND :end")
    List<MealLog> findAllByDailyIntakeSummary_UserAndMealDateBetween(@Param("user") User user, @Param("start") LocalDate start, @Param("end") LocalDate end);

    List<MealLog> findAllByMealDate(LocalDate date);

    @Query("SELECT ml FROM MealLog ml JOIN ml.dailyIntakeSummary dis WHERE dis.user = :user AND ml.mealDate = :date")
    List<MealLog> findAllByDailyIntakeSummary_UserAndMealDate(@Param("user") User user, @Param("date") LocalDate date);

    long countByDailyIntakeSummary(DailyIntakeSummary summary);
}