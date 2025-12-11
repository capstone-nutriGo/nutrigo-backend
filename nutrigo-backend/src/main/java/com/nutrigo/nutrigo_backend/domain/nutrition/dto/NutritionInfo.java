package com.nutrigo.nutrigo_backend.domain.nutrition.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NutritionInfo {

    @JsonProperty("kcal")
    private Float kcal;

    @JsonProperty("carb_g")
    private Float carbG;

    @JsonProperty("protein_g")
    private Float proteinG;

    @JsonProperty("fat_g")
    private Float fatG;

    @JsonProperty("sodium_mg")
    private Float sodiumMg;

    @JsonProperty("confidence")
    private Float confidence;
}
