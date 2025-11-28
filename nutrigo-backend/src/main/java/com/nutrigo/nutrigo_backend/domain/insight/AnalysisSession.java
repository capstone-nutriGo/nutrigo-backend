package com.nutrigo.nutrigo_backend.domain.insight;

import com.nutrigo.nutrigo_backend.domain.user.User;
import com.nutrigo.nutrigo_backend.global.common.enums.*;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "analysis_session")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnalysisSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "analysis_id")
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "type",
            nullable = false,
            columnDefinition = "ENUM('link','cart')")
    private AnalysisType type;

    @Enumerated(EnumType.STRING)
    @Column(name = "raw_input_type",
            nullable = false,
            columnDefinition = "ENUM('url','image')")
    private RawInputType rawInputType;

    @Column(name = "raw_input_value", nullable = false, columnDefinition = "TEXT")
    private String rawInputValue;

    @Column(name = "total_kcal")
    private Float totalKcal;

    @Column(name = "total_sodium_mg")
    private Float totalSodiumMg;

    @Column(name = "total_score")
    private Float totalScore;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "session_purpose",
            columnDefinition = "ENUM('PRE_ORDER','RECORD')")
    private SessionPurpose sessionPurpose;

    @Enumerated(EnumType.STRING)
    @Column(name = "meal_time",
            columnDefinition = "ENUM('BREAKFAST','LUNCH','DINNER','SNACK')")
    private MealTime mealTime;

    @Column(name = "meal_date")
    private LocalDate mealDate;
}
