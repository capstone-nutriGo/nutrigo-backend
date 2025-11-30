package com.nutrigo.nutrigo_backend.domain.insight;

import com.nutrigo.nutrigo_backend.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface DailyIntakeSummaryRepository extends JpaRepository<DailyIntakeSummary, Long> {

    Optional<DailyIntakeSummary> findByUserAndDate(User user, LocalDate date);

    Optional<DailyIntakeSummary> findByDate(LocalDate date);

    List<DailyIntakeSummary> findAllByUserAndDateBetween(User user, LocalDate start, LocalDate end);

    List<DailyIntakeSummary> findAllByDateBetween(LocalDate start, LocalDate end);

}
