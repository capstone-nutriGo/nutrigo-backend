package com.nutrigo.nutrigo_backend.domain.insight.dto;

public record MealAnalysisResult(
        String menu,
        String category,
        Float kcal,
        Float sodiumMg,
        Float proteinG,
        Float carbG,
        Float totalScore
) {
    public NutrientProfile nutrientProfile() {
        return new NutrientProfile(kcal, sodiumMg, proteinG, carbG);
    }
}