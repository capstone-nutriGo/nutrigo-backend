package com.nutrigo.nutrigo_backend.global.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class NutrigoAiClientConfig {

    @Bean
    public RestClient nutrigoAiRestClient(
            RestClient.Builder builder,
            @Value("${nutrigo.ai.base-url}") String baseUrl
    ) {
        return builder
                .baseUrl(baseUrl)
                .build();
    }
}
