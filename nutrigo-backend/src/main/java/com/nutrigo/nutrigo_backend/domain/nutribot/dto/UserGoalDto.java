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
public class UserGoalDto {
    @JsonProperty("focus")
    private String focus; // "diet", "bulk", "maintenance", "low_sodium", "custom"

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

