package com.nutrigo.nutrigo_backend.domain.nutribot.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class NutriBotChatRequest {
    @NotBlank(message = "메시지는 필수입니다")
    private String message;
}

