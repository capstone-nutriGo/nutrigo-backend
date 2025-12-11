package com.nutrigo.nutrigo_backend.domain.auth;

import com.nutrigo.nutrigo_backend.domain.auth.dto.AuthResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/auth/kakao")
@RequiredArgsConstructor
public class KakaoOauthController {

    private final KakaoOauthService kakaoOauthService;

    @GetMapping("/login")
    public ResponseEntity<Void> redirectToKakao() {
        String authorizeUri = kakaoOauthService.buildAuthorizeUri();
        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(authorizeUri))
                .build();
    }

    @GetMapping("/callback")
    public ResponseEntity<AuthResponse> kakaoCallback(@RequestParam("code") String code) {
        return ResponseEntity.ok(kakaoOauthService.handleCallback(code));
    }
}