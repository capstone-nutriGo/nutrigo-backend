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
}
