package com.nutrigo.nutrigo_backend.domain.user.dto;

import com.nutrigo.nutrigo_backend.global.common.enums.Gender;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record UserProfileUpdateRequest(
        @Size(min = 1, max = 50, message = "닉네임은 1자 이상 50자 이하여야 합니다")
        String nickname,
        @Size(min = 1, max = 50, message = "이름은 1자 이상 50자 이하여야 합니다")
        String name,
        Gender gender,
        @PastOrPresent(message = "생년월일은 과거 또는 오늘 날짜여야 합니다")
        LocalDate birthday
) {
}