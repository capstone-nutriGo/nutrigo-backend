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
    /*
        #swagger.summary = '맞춤 챌린지 생성'
        #swagger.description = '사용자 정의 기준으로 새로운 챌린지를 만들고 즉시 참여합니다.'
        #swagger.requestBody = {
          required: true,
          content: {
            "application/json": {
              schema: {
                type: "object",
                properties: {
                  title: { type: "string", description: "챌린지 제목" },
                  description: { type: "string", description: "상세 설명" },
                  category: { type: "string", description: "챌린지 카테고리" },
                  type: { type: "string", description: "챌린지 유형" },
                  durationDays: { type: "number", description: "진행 기간(일)" },
                  goal: {
                    type: "object",
                    description: "달성 목표 설정",
                    properties: {
                      targetCount: { type: "number", description: "목표 참여 횟수" },
                      maxKcalPerMeal: { type: "number", description: "한 끼당 최대 칼로리" },
                      maxSodiumMgPerMeal: { type: "number", description: "한 끼당 최대 나트륨(mg)" },
                      customDescription: { type: "string", description: "맞춤 목표 설명" }
                    }
                  }
                },
                required: ["title", "type", "durationDays"]
              }
            }
          }
        }
    */
    public ResponseEntity<ChallengeCreateResponse> createCustomChallenge(
            @RequestBody ChallengeCreateRequest request
    ) {
        return ResponseEntity.ok(challengeService.createCustomChallenge(request));
    }
}