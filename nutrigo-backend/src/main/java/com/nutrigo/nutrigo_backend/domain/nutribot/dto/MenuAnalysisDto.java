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
public class MenuAnalysisDto {
    @JsonProperty("menu")
    private MenuTextDto menu;

    @JsonProperty("nutrition")
    private NutritionEstimateDto nutrition;

    @JsonProperty("score")
    private Double score;

    @JsonProperty("badges")
    private List<String> badges;

    @JsonProperty("coach_sentence")
    private String coachSentence;
}

