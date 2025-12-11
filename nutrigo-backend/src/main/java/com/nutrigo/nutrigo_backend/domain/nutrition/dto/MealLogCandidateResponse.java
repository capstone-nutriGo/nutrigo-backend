package com.nutrigo.nutrigo_backend.domain.nutrition.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MealLogCandidateResponse {

    private String menu;
    private String category;

    private Float kcal;

    @JsonProperty("sodium_mg")
    private Float sodiumMg;

    @JsonProperty("protein_g")
    private Float proteinG;

    @JsonProperty("carb_g")
    private Float carbG;

    @JsonProperty("total_score")
    private Float totalScore;

    @JsonProperty("intake_min_ratio")
    private Float intakeMinRatio;

    @JsonProperty("intake_max_ratio")
    private Float intakeMaxRatio;

    @JsonProperty("intake_default_ratio")
    private Float intakeDefaultRatio;
}
