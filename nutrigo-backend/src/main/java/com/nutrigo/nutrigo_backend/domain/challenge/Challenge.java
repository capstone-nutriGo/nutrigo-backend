package com.nutrigo.nutrigo_backend.domain.challenge;

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
    @Column(name = "type", nullable = false,
            columnDefinition = "ENUM('kcal','sodium','frequency','day_color','delivery_count','custom')")
    private String type;

    @Column(name = "duration_days", nullable = false)
    private Integer durationDays;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    // ENUM('ACTIVE','INACTIVE')
    @Column(name = "status",
            columnDefinition = "ENUM('ACTIVE','INACTIVE')")
    private String status;

    // ENUM('HEALTH','FUN')
    @Column(name = "category",
            columnDefinition = "ENUM('HEALTH','FUN')")
    private String category;
}
