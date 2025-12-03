package com.nutrigo.nutrigo_backend.domain.insight;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.OffsetDateTime;
import java.util.List;

public interface MealLogRepository extends JpaRepository<MealLog, Long> {

    List<MealLog> findAllByOrderedAtBetween(OffsetDateTime start, OffsetDateTime end);
}
