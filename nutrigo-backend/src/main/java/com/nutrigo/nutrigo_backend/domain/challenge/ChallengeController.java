package com.nutrigo.nutrigo_backend.domain.challenge;

import com.nutrigo.nutrigo_backend.domain.challenge.dto.ChallengeListResponse;
import com.nutrigo.nutrigo_backend.domain.challenge.dto.ChallengeProgressResponse;
import com.nutrigo.nutrigo_backend.domain.challenge.dto.ChallengeQuitResponse;
import com.nutrigo.nutrigo_backend.domain.challenge.dto.JoinChallengeResponse;
import com.nutrigo.nutrigo_backend.domain.challenge.dto.ChallengeProgressDetailResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/challenges")
@RequiredArgsConstructor
public class ChallengeController {

    private final ChallengeService challengeService;

    @GetMapping
    public ResponseEntity<ChallengeListResponse> getChallenges(@RequestParam(required = false) String status) {
        return ResponseEntity.ok(challengeService.getChallenges(status));
    }

    @PostMapping("/{challengeId}/join")
    public ResponseEntity<JoinChallengeResponse> joinChallenge(@PathVariable Long challengeId) {
        return ResponseEntity.ok(challengeService.joinChallenge(challengeId));
    }

    @PostMapping("/{challengeId}/quit")
    public ResponseEntity<ChallengeQuitResponse> quitChallenge(@PathVariable Long challengeId) {
        return ResponseEntity.ok(challengeService.quitChallenge(challengeId));
    }

    @GetMapping("/progress")
    public ResponseEntity<ChallengeProgressResponse> getProgress() {
        return ResponseEntity.ok(challengeService.getProgress());
    }

    @GetMapping("/{challengeId}/progress")
    public ResponseEntity<ChallengeProgressDetailResponse> getChallengeProgress(@PathVariable Long challengeId) {
        return ResponseEntity.ok(challengeService.getChallengeProgress(challengeId));
    }
}