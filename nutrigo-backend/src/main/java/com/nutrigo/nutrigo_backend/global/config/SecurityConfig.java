package com.nutrigo.nutrigo_backend.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
public class SecurityConfig {

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // ✅ 1) CORS 활성화
                .cors(Customizer.withDefaults())

                // ✅ 2) CSRF 비활성화 (API 서버일 경우 보통 disable)
                .csrf(AbstractHttpConfigurer::disable)

                // H2 콘솔이 frame 안에서 열릴 수 있도록 설정
                .headers(headers -> headers
                        .frameOptions(frame -> frame.sameOrigin())
                )

                // ✅ 3) 경로별 권한 설정
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/h2-console/**").permitAll()

                        // 임시 개발용 insight 로그
                        .requestMatchers(HttpMethod.POST, "/api/v1/insights/logs").permitAll()

                        // ✅ auth 도메인: 회원가입/로그인 등은 모두 허용
                        .requestMatchers("/api/v1/auth/**").permitAll()

                        // 나머지는 일단 모두 허용 (나중에 authenticated()로 바꿀 수 있음)
                        .anyRequest().permitAll()
                );

        return http.build();
    }

    // ✅ 4) CORS 설정 Bean: React 개발 서버에서 오는 요청 허용
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // 프론트 주소 (개발 환경)
        configuration.setAllowedOrigins(List.of("http://localhost:3000"));

        // 허용할 HTTP 메서드
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));

        // 허용할 헤더
        configuration.setAllowedHeaders(List.of("*"));

        // 쿠키/Authorization 헤더를 허용할지 여부 (JWT 쓸 거면 true)
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // 모든 경로에 위 설정 적용
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
