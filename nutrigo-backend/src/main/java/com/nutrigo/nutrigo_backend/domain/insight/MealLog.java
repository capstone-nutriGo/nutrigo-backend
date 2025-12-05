package com.nutrigo.nutrigo_backend.domain.insight;

import com.nutrigo.nutrigo_backend.global.common.enums.MealTime;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
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

    @Column(name = "menu", length = 50)
    private String menu;

    @Column(name = "kcal")
    private Float kcal;

    @Column(name = "sodium_mg")
    private Float sodiumMg;

    @Column(name = "protein_g")
    private Float proteinG;

    @Column(name = "carb_g")
    private Float carbG;

    @Column(name = "total_score")
    private Float totalScore;

    @Enumerated(EnumType.STRING)
    @Column(name = "meal_time", columnDefinition = "ENUM('BREAKFAST','LUNCH','DINNER','SNACK','NIGHT')")
    private MealTime mealTime;

    @Column(name = "meal_date")
    private LocalDate mealDate;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "daily_intake_summary_id", nullable = false)
    private DailyIntakeSummary dailyIntakeSummary;
}