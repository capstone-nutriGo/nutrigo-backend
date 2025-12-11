package com.nutrigo.nutrigo_backend.domain.nutribot.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailySummaryDto {
    @JsonProperty("date")
    private LocalDate date;

    @JsonProperty("total_kcal")
    private Double totalKcal;

    @JsonProperty("total_sodium_mg")
    private Double totalSodiumMg;

    @JsonProperty("total_protein_g")
    private Double totalProteinG;

    @JsonProperty("total_fat_g")
    private Double totalFatG;

    @JsonProperty("total_carb_g")
    private Double totalCarbG;
}

