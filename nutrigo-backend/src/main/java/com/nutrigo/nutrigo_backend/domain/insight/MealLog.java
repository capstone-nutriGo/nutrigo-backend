package com.nutrigo.nutrigo_backend.domain.insight;

import com.nutrigo.nutrigo_backend.global.common.enums.MealTime;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;

@Entity
@Table(name = "meal_log")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MealLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "meal_log_id")
    private Long id;

    @Column(name = "source", nullable = false, length = 50)
    private String source;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "analysis_id", nullable = false)
    private AnalysisSession analysisSession;

    @Enumerated(EnumType.STRING)
    @Column(name = "meal_time", columnDefinition = "ENUM('BREAKFAST','LUNCH','DINNER','SNACK')")
    private MealTime mealTime;

    @Column(name = "ordered_at", nullable = false)
    private OffsetDateTime orderedAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}