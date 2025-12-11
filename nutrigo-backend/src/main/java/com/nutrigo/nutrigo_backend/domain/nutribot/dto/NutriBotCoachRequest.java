package com.nutrigo.nutrigo_backend.domain.nutribot.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NutriBotCoachRequest {
    @NotNull
    @JsonProperty("mode")
    private String mode; // "today" or "chat"

    @NotNull
    @JsonProperty("user_goal")
    private UserGoalDto userGoal;

    @JsonProperty("daily_summaries")
    private List<DailySummaryDto> dailySummaries;

    @JsonProperty("recent_menus")
    private List<MenuAnalysisDto> recentMenus;

    @JsonProperty("user_message")
    private String userMessage; // chat 모드일 때 사용자의 질문
}

