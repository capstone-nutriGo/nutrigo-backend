package com.nutrigo.nutrigo_backend.domain.nutribot.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NutriBotChatResponse {
    private String reply;
    private String tone; // "gentle" | "strict" | "motivational"
    
    @JsonProperty("recommended_actions")
    private List<String> recommendedActions;
}

