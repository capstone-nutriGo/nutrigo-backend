package com.nutrigo.nutrigo_backend.domain.nutrition.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MenuAnalysis {

    @JsonProperty("menu")
    private MenuInfo menu;

    @JsonProperty("nutrition")
    private NutritionInfo nutrition;

    @JsonProperty("score")
    private Double score;

    @JsonProperty("badges")
    private List<String> badges;

    @JsonProperty("coach_sentence")
    private String coachSentence;
}
