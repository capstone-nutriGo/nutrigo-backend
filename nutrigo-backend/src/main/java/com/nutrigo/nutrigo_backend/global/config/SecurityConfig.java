package com.nutrigo.nutrigo_backend.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {
    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                
                // H2 콘솔은 CSRF 예외 처리
//            .csrf(csrf -> csrf
//                    .ignoringRequestMatchers("/h2-console/**")
//            )
            // H2 콘솔이 frame 안에서 열릴 수 있도록 설정
            .headers(headers -> headers
                    .frameOptions(frame -> frame.sameOrigin())
            )
                .authorizeHttpRequests(auth -> auth
                    
                    .requestMatchers("/h2-console/**").permitAll()
                    //임시 개발용
                    .requestMatchers(HttpMethod.POST, "/api/v1/insights/logs").permitAll()

                        .anyRequest().permitAll());
        return http.build();
    }
}