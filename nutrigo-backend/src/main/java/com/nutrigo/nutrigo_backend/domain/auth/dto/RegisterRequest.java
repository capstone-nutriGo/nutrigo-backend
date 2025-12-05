package com.nutrigo.nutrigo_backend.domain.auth.dto;

import com.nutrigo.nutrigo_backend.global.common.enums.Gender;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PastOrPresent;

import java.time.LocalDate;

public record RegisterRequest(
        @NotBlank(message = "이메일을 입력해주세요")
        @Email(message = "이메일 형식이 올바르지 않습니다")
        String email,
        @NotBlank(message = "비밀번호를 입력해주세요")
        String password,
        @NotBlank(message = "닉네임을 입력해주세요")
        String nickname,
        @NotBlank(message = "이름을 입력해주세요")
        String name,
        Gender gender,
        @PastOrPresent(message = "생년월일은 과거 또는 오늘 날짜여야 합니다")
        LocalDate birthday
) {
}