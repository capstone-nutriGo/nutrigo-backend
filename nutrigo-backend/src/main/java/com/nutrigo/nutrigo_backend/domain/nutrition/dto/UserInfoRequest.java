package com.nutrigo.nutrigo_backend.domain.nutrition.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.nutrigo.nutrigo_backend.domain.user.User;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserInfoRequest {

    @NotNull
    @JsonProperty("gender")
    private String gender;   // "male", "female", "other"

    @NotNull
    @JsonProperty("birthday")
    private LocalDate birthday;

    // 선택: User 엔티티에서 바로 만들 수 있게 팩토리 메서드 하나 두면 편함
    public static UserInfoRequest from(User user) {
        if (user == null) {
            return null;
        }
        String gender = user.getGender() != null
                ? user.getGender().name().toLowerCase()   // enum → "male" / "female" / "other"
                : "other";

        return UserInfoRequest.builder()
                .gender(gender)
                .birthday(user.getBirthday())
                .build();
    }
}
