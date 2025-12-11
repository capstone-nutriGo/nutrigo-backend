package com.nutrigo.nutrigo_backend.domain.nutrition.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserGoalRequest {

    // diet / bulk / maintenance / low_sodium / custom ...
    @NotBlank
    private String focus;

    @JsonProperty("calorie_min")
    private Integer calorieMin;

    @JsonProperty("calorie_max")
    private Integer calorieMax;

    @JsonProperty("protein_min")
    private Integer proteinMin;

    @JsonProperty("fat_max")
    private Integer fatMax;

    @JsonProperty("carb_max")
    private Integer carbMax;

    @JsonProperty("sodium_max")
    private Integer sodiumMax;
}
