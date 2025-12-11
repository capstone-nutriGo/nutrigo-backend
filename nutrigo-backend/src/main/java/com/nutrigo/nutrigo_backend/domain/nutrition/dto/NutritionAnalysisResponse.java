package com.nutrigo.nutrigo_backend.domain.nutrition.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NutritionAnalysisResponse {

    @JsonProperty("analyses")
    private List<MenuAnalysis> analyses;

    @JsonProperty("summary")
    private String summary;

    @JsonProperty("recommended_menu_ids")
    private List<String> recommendedMenuIds;
}
