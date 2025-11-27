package com.nutrigo.nutrigo_backend.domain.insight;

import com.nutrigo.nutrigo_backend.domain.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "daily_intake_summary")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DailyIntakeSummary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "daily_intake_summary_id") // 타이포 그대로 매핑
    private Long id;

    @Column(nullable = false)
    private LocalDate date;

    @Column(name = "total_kcal")
    private Float totalKcal;

    @Column(name = "total_sodium_mg")
    private Float totalSodiumMg;

    @Column(name = "total_protein_g")
    private Float totalProteinG;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "total_meals")
    private Integer totalMeals;

    @Column(name = "is_good_day")
    private Boolean goodDay;

    @Column(name = "is_overeat_day")
    private Boolean overeatDay;

    @Column(name = "is_low_sodium_day")
    private Boolean lowSodiumDay;

    @Column(name = "day_score")
    private Float dayScore;
}
