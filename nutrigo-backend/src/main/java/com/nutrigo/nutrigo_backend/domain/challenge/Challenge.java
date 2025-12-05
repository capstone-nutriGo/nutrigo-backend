package com.nutrigo.nutrigo_backend.domain.challenge;

import com.nutrigo.nutrigo_backend.domain.user.User;
import com.nutrigo.nutrigo_backend.global.common.enums.ChallengeCategory;
import com.nutrigo.nutrigo_backend.global.common.enums.ChallengeType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "challenge")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Challenge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "challenge_id")
    private Long id;

    @Column(nullable = false, length = 50)
    private String code;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    // ENUM('kcal','sodium','frequency','day_color','delivery_count','custom')
    @Enumerated(EnumType.STRING)
    @Column(name = "category")
    private ChallengeCategory category;

    @Column(name = "duration_days", nullable = false)
    private Integer durationDays;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    // ENUM('ACTIVE','INACTIVE')
    @Column(name = "status",
            columnDefinition = "ENUM('ACTIVE','INACTIVE')")
    private String status;

    // ENUM('HEALTH','FUN')
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private ChallengeType type;

    @Column(name = "is_custom")
    private Boolean custom;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

    @Column(name = "target_count")
    private Integer targetCount;

    @Column(name = "max_kcal_per_meal")
    private Integer maxKcalPerMeal;

    @Column(name = "max_sodium_mg_per_meal")
    private Integer maxSodiumMgPerMeal;

    @Column(name = "custom_description", columnDefinition = "TEXT")
    private String customDescription;
}
