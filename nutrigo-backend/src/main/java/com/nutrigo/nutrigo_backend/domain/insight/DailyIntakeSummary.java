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

    @Column(name = "total_carb_g")
    private Float totalCarbG;

    @Column(name = "total_snack")
    private Integer totalSnack;

    @Column(name = "total_night")
    private Integer totalNight;

    @Column(name = "day_score")
    private Float dayScore;

    @Column(name = "day_color", columnDefinition = "ENUM('red','yellow','green')")
    private String dayColor;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}
