package com.nutrigo.nutrigo_backend.domain.challenge;

import com.nutrigo.nutrigo_backend.domain.challenge.dto.ChallengeListResponse;
import com.nutrigo.nutrigo_backend.domain.challenge.dto.ChallengeProgressResponse;
import com.nutrigo.nutrigo_backend.domain.challenge.dto.JoinChallengeResponse;
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
    /*
        #swagger.summary = '챌린지 목록 조회'
        #swagger.description = '모든 챌린지 또는 상태 필터(available, in-progress, done)에 맞는 챌린지를 조회합니다.'
        #swagger.parameters['status'] = {
          in: 'query',
          required: false,
          description: '필터링할 챌린지 상태',
          type: 'string'
        }
    */
    public ResponseEntity<ChallengeListResponse> getChallenges(@RequestParam(required = false) String status) {
        return ResponseEntity.ok(challengeService.getChallenges(status));
    }

    @PostMapping("/{challengeId}/join")
    /*
        #swagger.summary = '챌린지 참여'
        #swagger.description = '선택한 챌린지에 사용자를 참여시키고 진행 상태를 생성합니다.'
        #swagger.parameters['challengeId'] = {
          in: 'path',
          required: true,
          description: '참여할 챌린지 ID',
          type: 'number'
        }
    */
    public ResponseEntity<JoinChallengeResponse> joinChallenge(@PathVariable Long challengeId) {
        return ResponseEntity.ok(challengeService.joinChallenge(challengeId));
    }

    @GetMapping("/progress")
    /*
        #swagger.summary = '챌린지 진행 현황'
        #swagger.description = '진행 중인 챌린지와 완료된 챌린지의 현황을 조회합니다.'
    */
    public ResponseEntity<ChallengeProgressResponse> getProgress() {
        return ResponseEntity.ok(challengeService.getProgress());
    }
}