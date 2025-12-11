package com.nutrigo.nutrigo_backend.domain.nutribot.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NutriBotCoachResponse {
    @JsonProperty("reply")
    private String reply;

    @JsonProperty("tone")
    private String tone; // "gentle", "strict", "motivational"

    @JsonProperty("recommended_actions")
    private List<String> recommendedActions;
}

