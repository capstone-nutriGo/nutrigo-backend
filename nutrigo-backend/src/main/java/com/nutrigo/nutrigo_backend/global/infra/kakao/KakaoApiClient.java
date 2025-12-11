package com.nutrigo.nutrigo_backend.global.infra.kakao;

import com.nutrigo.nutrigo_backend.global.infra.kakao.dto.KakaoUserResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class KakaoApiClient {

    private static final String USER_ME_PATH = "/v2/user/me";
    private static final String LOGOUT_PATH = "/v1/user/logout";

    private final RestClient restClient;

    public KakaoApiClient(RestClient.Builder restClientBuilder) {
        this.restClient = restClientBuilder.baseUrl("https://kapi.kakao.com").build();
    }

    public KakaoUserResponse getUserProfile(String accessToken) {
        return restClient.get()
                .uri(USER_ME_PATH)
                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                .body(KakaoUserResponse.class);
    }

    public void logout(String accessToken) {
        restClient.post()
                .uri(LOGOUT_PATH)
                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                .toBodilessEntity();
    }
}