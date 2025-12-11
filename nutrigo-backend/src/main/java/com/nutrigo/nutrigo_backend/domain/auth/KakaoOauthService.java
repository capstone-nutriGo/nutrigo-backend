package com.nutrigo.nutrigo_backend.domain.auth;

import com.nutrigo.nutrigo_backend.domain.auth.dto.AuthResponse;
import com.nutrigo.nutrigo_backend.domain.auth.dto.SocialLoginRequest;
import com.nutrigo.nutrigo_backend.global.infra.kakao.KakaoApiClient;
import com.nutrigo.nutrigo_backend.global.infra.kakao.KakaoAuthClient;
import com.nutrigo.nutrigo_backend.global.infra.kakao.dto.KakaoTokenResponse;
import com.nutrigo.nutrigo_backend.global.infra.kakao.dto.KakaoUserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class KakaoOauthService {

    @Value("${kakao.oauth.client-id}")
    private String clientId;

    @Value("${kakao.oauth.client-secret:}")
    private String clientSecret;

    @Value("${kakao.oauth.redirect-uri}")
    private String redirectUri;

    private final KakaoAuthClient kakaoAuthClient;
    private final KakaoApiClient kakaoApiClient;
    private final AuthService authService;

    public String buildAuthorizeUri() {
        return "https://kauth.kakao.com/oauth/authorize" +
                "?response_type=code" +
                "&client_id=" + clientId +
                "&redirect_uri=" + redirectUri;
    }

    public AuthResponse handleCallback(String code) {
        KakaoTokenResponse kakaoToken = kakaoAuthClient.exchangeCodeForToken(clientId, redirectUri, code, clientSecret);
        KakaoUserResponse userResponse = kakaoApiClient.getUserProfile(kakaoToken.getAccessToken());

        String email = resolveEmail(userResponse);
        String nickname = resolveNickname(userResponse);

        SocialLoginRequest socialLoginRequest = new SocialLoginRequest(
                SocialLoginRequest.Provider.KAKAO,
                kakaoToken.getAccessToken(),
                kakaoToken.getIdToken(),
                null
        );

        return authService.socialLogin(socialLoginRequest, email, nickname);
    }

    private String resolveEmail(KakaoUserResponse userResponse) {
        if (userResponse.getKakaoAccount() != null && StringUtils.hasText(userResponse.getKakaoAccount().getEmail())) {
            return userResponse.getKakaoAccount().getEmail();
        }
        return "kakao_" + userResponse.getId() + "@kakao.com";
    }

    private String resolveNickname(KakaoUserResponse userResponse) {
        if (userResponse.getKakaoAccount() != null &&
                userResponse.getKakaoAccount().getProfile() != null &&
                StringUtils.hasText(userResponse.getKakaoAccount().getProfile().getNickname())) {
            return userResponse.getKakaoAccount().getProfile().getNickname();
        }
        return "카카오 유저";
    }
}