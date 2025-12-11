package com.nutrigo.nutrigo_backend.global.infra.kakao;

import com.nutrigo.nutrigo_backend.global.infra.kakao.dto.KakaoTokenResponse;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

@Component
public class KakaoAuthClient {

    private static final String TOKEN_PATH = "/oauth/token";

    private final RestClient restClient;

    public KakaoAuthClient(RestClient.Builder restClientBuilder) {
        this.restClient = restClientBuilder.baseUrl("https://kauth.kakao.com").build();
    }

    public KakaoTokenResponse exchangeCodeForToken(String clientId,
                                                   String redirectUri,
                                                   String code,
                                                   String clientSecret) {
        MultiValueMap<String, String> payload = new LinkedMultiValueMap<>();
        payload.add("grant_type", "authorization_code");
        payload.add("client_id", clientId);
        payload.add("redirect_uri", redirectUri);
        payload.add("code", code);
        if (clientSecret != null && !clientSecret.isBlank()) {
            payload.add("client_secret", clientSecret);
        }

        return restClient.post()
                .uri(TOKEN_PATH)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(payload)
                .retrieve()
                .body(KakaoTokenResponse.class);
    }
}