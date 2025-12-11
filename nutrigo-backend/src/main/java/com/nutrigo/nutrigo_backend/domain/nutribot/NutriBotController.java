package com.nutrigo.nutrigo_backend.domain.nutribot;

import com.nutrigo.nutrigo_backend.domain.nutribot.dto.NutriBotCoachResponse;
import com.nutrigo.nutrigo_backend.global.response.ApiResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/nutribot")
@RequiredArgsConstructor
public class NutriBotController {

    private final NutriBotService nutriBotService;

    @PostMapping("/chat")
    public ResponseEntity<ApiResponse<NutriBotCoachResponse>> chat(
            @Valid @RequestBody ChatRequest request,
            @RequestHeader(value = "Authorization", required = false) String authorization
    ) {
        NutriBotCoachResponse response = nutriBotService.chat(request.getMessage(), authorization);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChatRequest {
        @NotBlank
        private String message;
    }
}

