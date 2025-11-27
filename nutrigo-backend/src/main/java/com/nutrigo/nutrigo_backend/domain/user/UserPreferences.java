package com.nutrigo.nutrigo_backend.domain.user;

import com.nutrigo.nutrigo_backend.global.common.enums.DefaultMode;
import com.nutrigo.nutrigo_backend.global.common.enums.HealthMode;
import com.nutrigo.nutrigo_backend.global.common.enums.PortionPreference;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_preferences")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserPreferences {

    @Id
    @Column(name = "user_id")
    private Long id;

    @OneToOne
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "portion_preference",
            columnDefinition = "ENUM('SMALL','NORMAL','LARGE')")
    private PortionPreference portionPreference;

    @Enumerated(EnumType.STRING)
    @Column(name = "health_mode",
            columnDefinition = "ENUM('RELAXED','NORMAL','STRICT')")
    private HealthMode healthMode;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "evening_coach")
    private Boolean eveningCoach;

    @Column(name = "challenge_reminder")
    private Boolean challengeReminder;

    @Enumerated(EnumType.STRING)
    @Column(name = "default_mode",
            columnDefinition = "ENUM('STRICT','BALANCED','FLEX')")
    private DefaultMode defaultMode;
}
