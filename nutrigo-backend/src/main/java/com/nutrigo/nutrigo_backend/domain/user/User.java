package com.nutrigo.nutrigo_backend.domain.user;

import com.nutrigo.nutrigo_backend.global.common.enums.Gender;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "`user`") // user는 예약어라 백틱 추천
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // PK는 AUTO_INCREMENT 가정
    @Column(name = "user_id")
    private Long id;

    @Column(nullable = false, length = 100, unique = true)
    private String email;

    @Column(nullable = false, length = 255)
    private String password;

    @Column(length = 50)
    private String nickname;

    @Column(length = 30)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "ENUM('male','female','other')")
    private Gender gender;

    private LocalDate birthday;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @OneToOne(mappedBy = "user", fetch = FetchType.LAZY)
    private UserSetting preferences;
}
