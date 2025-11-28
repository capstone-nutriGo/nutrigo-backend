package com.nutrigo.nutrigo_backend.domain.insight;

import com.nutrigo.nutrigo_backend.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface AnalysisSessionRepository extends JpaRepository<AnalysisSession, Long> {

    List<AnalysisSession> findAllByUserAndCreatedAtBetween(User user,
                                                           LocalDateTime from,
                                                           LocalDateTime to);
}
