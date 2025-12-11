package com.nutrigo.nutrigo_backend.domain.challenge.dto;

import com.nutrigo.nutrigo_backend.global.common.enums.ChallengeType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ChallengeCreateRequest(
        @NotBlank(message = "챌린지 제목을 입력해주세요")
        String title,
        String description,
        @NotNull(message = "챌린지 유형을 선택해주세요")
        ChallengeType type,
        @NotNull(message = "챌린지 기간을 입력해주세요")
        @Positive(message = "기간은 1일 이상이어야 합니다")
        Integer durationDays
) {
}