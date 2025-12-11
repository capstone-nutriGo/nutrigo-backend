package com.nutrigo.nutrigo_backend.domain.auth;

import com.nutrigo.nutrigo_backend.domain.auth.dto.AuthResponse;
import com.nutrigo.nutrigo_backend.domain.auth.dto.LogoutRequest;
import com.nutrigo.nutrigo_backend.domain.auth.dto.SocialLoginRequest;
import com.nutrigo.nutrigo_backend.global.infra.kakao.KakaoApiClient;
import com.nutrigo.nutrigo_backend.global.infra.kakao.KakaoAuthClient;
import com.nutrigo.nutrigo_backend.global.infra.kakao.dto.KakaoTokenResponse;
import com.nutrigo.nutrigo_backend.global.infra.kakao.dto.KakaoUserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UriComponentsBuilder;

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
    private final ObjectMapper objectMapper;

    public String buildAuthorizeUri(String state) {
        UriComponentsBuilder builder = UriComponentsBuilder
                .fromHttpUrl("https://kauth.kakao.com/oauth/authorize")
                .queryParam("response_type", "code")
                .queryParam("client_id", clientId)
                .queryParam("redirect_uri", redirectUri);

        if (StringUtils.hasText(state)) {
            builder.queryParam("state", state);
        }

        return builder.toUriString();
    }

    public String buildAuthorizeUri() {
        return buildAuthorizeUri(null);
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

        AuthResponse authResponse = authService.socialLogin(socialLoginRequest, email, nickname);
        return authResponse.appendProviderAccessToken(kakaoToken.getAccessToken());
    }

    public LogoutRequest.Response logout(String authorizationHeader) {
        String kakaoAccessToken = extractBearerToken(authorizationHeader);
        boolean hasToken = StringUtils.hasText(kakaoAccessToken);
        if (hasToken) {
            kakaoApiClient.logout(kakaoAccessToken);
        }
        return new LogoutRequest.Response(true, new LogoutRequest.Data(hasToken));
    }

    public String buildLoginBridgeHtml(AuthResponse authResponse) {
        try {
            String json = objectMapper.writeValueAsString(authResponse);
            return """
                    <!doctype html>
                    <html lang=\"ko\">
                    <head>
                        <meta charset=\"UTF-8\" />
                        <title>Kakao Login 완료</title>
                        <meta name=\"viewport\" content=\"width=device-width, initial-scale=1\" />
                        <style>
                            body { font-family: Arial, sans-serif; display: flex; justify-content: center; align-items: center; min-height: 100vh; background: #f9f9f9; }
                            .box { padding: 24px; border: 1px solid #ddd; border-radius: 8px; background: #fff; box-shadow: 0 2px 6px rgba(0,0,0,0.08); max-width: 420px; text-align: center; }
                            h2 { margin-top: 0; }
                        </style>
                    </head>
                    <body>
                        <div class=\"box\">
                            <h2>로그인이 완료되었습니다</h2>
                            <p>잠시 후 테스트 페이지로 돌아갑니다.</p>
                        </div>
                        <script>
                            const authResponse = %s;
                            try {
                                if (authResponse && authResponse.data) {
                                    localStorage.setItem('kakaoAuthResponse', JSON.stringify(authResponse));
                                    localStorage.setItem('kakaoProviderAccessToken', authResponse.data.providerAccessToken || '');
                                    window.postMessage({ type: 'kakaoLogin', payload: authResponse }, window.location.origin);
                                }
                            } catch (e) {
                                console.error('Failed to store login data', e);
                            }
                            setTimeout(() => {
                                window.location.replace('/login/kakao');
                            }, 700);
                        </script>
                    </body>
                    </html>
                    """.formatted(json);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize auth response", e);
        }
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

    private String extractBearerToken(String authorizationHeader) {
        if (!StringUtils.hasText(authorizationHeader)) {
            return null;
        }
        String lower = authorizationHeader.toLowerCase();
        if (lower.startsWith("bearer ")) {
            return authorizationHeader.substring(7);
        }
        return authorizationHeader;
    }
}