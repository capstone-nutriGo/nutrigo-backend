package com.nutrigo.nutrigo_backend.domain.nutribot;

import com.nutrigo.nutrigo_backend.domain.nutribot.dto.NutriBotChatRequest;
import com.nutrigo.nutrigo_backend.domain.nutribot.dto.NutriBotChatResponse;
import com.nutrigo.nutrigo_backend.global.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/nutribot")
@RequiredArgsConstructor
public class NutriBotController {

    private final NutriBotService nutriBotService;

    @PostMapping("/chat")
    public ResponseEntity<ApiResponse<NutriBotChatResponse>> chat(
            @Valid @RequestBody NutriBotChatRequest request
    ) {
        NutriBotChatResponse response = nutriBotService.chat(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}

