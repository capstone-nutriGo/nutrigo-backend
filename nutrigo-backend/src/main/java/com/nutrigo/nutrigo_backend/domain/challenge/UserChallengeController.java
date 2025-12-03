package com.nutrigo.nutrigo_backend.domain.challenge;

import com.nutrigo.nutrigo_backend.domain.challenge.dto.ChallengeCreateRequest;
import com.nutrigo.nutrigo_backend.domain.challenge.dto.ChallengeCreateResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users/me/challenges")
@RequiredArgsConstructor
public class UserChallengeController {

    private final ChallengeService challengeService;

    @PostMapping
    public ResponseEntity<ChallengeCreateResponse> createCustomChallenge(
            @RequestBody ChallengeCreateRequest request
    ) {
        return ResponseEntity.ok(challengeService.createCustomChallenge(request));
    }
}
