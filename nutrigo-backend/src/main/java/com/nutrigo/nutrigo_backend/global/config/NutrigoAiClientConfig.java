package com.nutrigo.nutrigo_backend.global.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class NutrigoAiClientConfig {

    @Bean
    public RestTemplate nutrigoAiRestTemplate(
            RestTemplateBuilder builder,
            @Value("${nutrigo.ai.base-url}") String baseUrl
    ) {
        // base-url 예: http://localhost:8000
        return builder
                .rootUri(baseUrl)
                .build();
    }
}
