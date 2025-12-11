package com.nutrigo.nutrigo_backend.domain.nutribot.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NutritionEstimateDto {
    @JsonProperty("kcal")
    private Double kcal;

    @JsonProperty("carb_g")
    private Double carbG;

    @JsonProperty("protein_g")
    private Double proteinG;

    @JsonProperty("fat_g")
    private Double fatG;

    @JsonProperty("sodium_mg")
    private Double sodiumMg;

    @JsonProperty("confidence")
    private Double confidence;
}

