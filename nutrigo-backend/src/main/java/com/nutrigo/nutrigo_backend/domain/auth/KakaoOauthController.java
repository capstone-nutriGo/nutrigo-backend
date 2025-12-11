package com.nutrigo.nutrigo_backend.domain.auth;

import com.nutrigo.nutrigo_backend.domain.auth.dto.AuthResponse;
import com.nutrigo.nutrigo_backend.domain.auth.dto.LogoutRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
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
    public ResponseEntity<Void> redirectToKakao(@RequestParam(value = "state", required = false) String state) {
        String authorizeUri = kakaoOauthService.buildAuthorizeUri(state);
        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(authorizeUri))
                .build();
    }

    @GetMapping("/callback")
    public ResponseEntity<?> kakaoCallback(@RequestParam("code") String code,
                                           @RequestParam(value = "state", required = false) String state) {
        AuthResponse response = kakaoOauthService.handleCallback(code);
        if ("login-view".equals(state)) {
            return ResponseEntity.ok()
                    .contentType(MediaType.TEXT_HTML)
                    .body(kakaoOauthService.buildLoginBridgeHtml(response));
        }
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<LogoutRequest.Response> kakaoLogout(
            @RequestHeader(value = "Authorization", required = false) String authorization
    ) {
        return ResponseEntity.ok(kakaoOauthService.logout(authorization));
    }
}