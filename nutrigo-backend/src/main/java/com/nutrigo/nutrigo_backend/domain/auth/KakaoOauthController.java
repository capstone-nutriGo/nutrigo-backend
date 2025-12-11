package com.nutrigo.nutrigo_backend.domain.auth;

import com.nutrigo.nutrigo_backend.domain.auth.dto.AuthResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/v1/auth/kakao")
@RequiredArgsConstructor
public class KakaoOauthController {

    private final KakaoOauthService kakaoOauthService;

    @Value("${frontend.url:http://localhost:3000}")
    private String frontendUrl;

    @GetMapping("/login")
    public ResponseEntity<Void> redirectToKakao() {
        String authorizeUri = kakaoOauthService.buildAuthorizeUri();
        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(authorizeUri))
                .build();
    }

    @GetMapping("/callback")
    public ResponseEntity<Void> kakaoCallback(@RequestParam("code") String code) {
        try {
            AuthResponse authResponse = kakaoOauthService.handleCallback(code);
            
            // 프론트엔드로 리다이렉트하면서 토큰 정보를 쿼리 파라미터로 전달
            String redirectUri = UriComponentsBuilder.fromUriString(frontendUrl + "/auth/kakao/callback")
                    .queryParam("success", "true")
                    .queryParam("accessToken", authResponse.data().accessToken())
                    .queryParam("refreshToken", authResponse.data().refreshToken())
                    .queryParam("userId", authResponse.data().user().userId())
                    .queryParam("email", URLEncoder.encode(authResponse.data().user().email(), StandardCharsets.UTF_8))
                    .queryParam("nickname", URLEncoder.encode(authResponse.data().user().nickname(), StandardCharsets.UTF_8))
                    .queryParam("isNewUser", authResponse.isNewUser() != null && authResponse.isNewUser())
                    .build()
                    .toUriString();
            
            return ResponseEntity.status(HttpStatus.FOUND)
                    .location(URI.create(redirectUri))
                    .build();
        } catch (Exception e) {
            // 에러 발생 시 프론트엔드로 에러 정보와 함께 리다이렉트
            String redirectUri = UriComponentsBuilder.fromUriString(frontendUrl + "/auth/kakao/callback")
                    .queryParam("success", "false")
                    .queryParam("error", URLEncoder.encode(e.getMessage() != null ? e.getMessage() : "카카오 로그인에 실패했습니다.", StandardCharsets.UTF_8))
                    .build()
                    .toUriString();
            
            return ResponseEntity.status(HttpStatus.FOUND)
                    .location(URI.create(redirectUri))
                    .build();
        }
    }
}