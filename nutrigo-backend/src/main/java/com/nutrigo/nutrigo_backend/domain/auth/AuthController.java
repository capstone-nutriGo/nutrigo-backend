package com.nutrigo.nutrigo_backend.domain.auth;

import com.nutrigo.nutrigo_backend.domain.auth.dto.AuthResponse;
import com.nutrigo.nutrigo_backend.domain.auth.dto.LoginRequest;
import com.nutrigo.nutrigo_backend.domain.auth.dto.LogoutRequest;
import com.nutrigo.nutrigo_backend.domain.auth.dto.RefreshRequest;
import com.nutrigo.nutrigo_backend.domain.auth.dto.RegisterRequest;
import com.nutrigo.nutrigo_backend.domain.auth.dto.SocialLoginRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    /*
        #swagger.summary = '회원 가입'
        #swagger.description = '이메일과 기본 정보를 이용해 신규 사용자를 등록합니다.'
        #swagger.requestBody = {
          required: true,
          content: {
            "application/json": {
              schema: {
                type: "object",
                properties: {
                  email: { type: "string", description: "로그인에 사용할 이메일" },
                  password: { type: "string", description: "계정 비밀번호" },
                  nickname: { type: "string", description: "서비스 내 표시 이름" },
                  name: { type: "string", description: "실제 이름" },
                  gender: { type: "string", description: "성별 (MALE/FEMALE)" },
                  birthday: { type: "string", description: "생년월일 (YYYY-MM-DD)" }
                },
                required: ["email", "password", "nickname"]
              }
            }
          }
        }
    */
    public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    /*
        #swagger.summary = '로그인'
        #swagger.description = '이메일과 비밀번호로 액세스 토큰과 리프레시 토큰을 발급합니다.'
        #swagger.requestBody = {
          required: true,
          content: {
            "application/json": {
              schema: {
                type: "object",
                properties: {
                  email: { type: "string", description: "회원 이메일" },
                  password: { type: "string", description: "계정 비밀번호" }
                },
                required: ["email", "password"]
              }
            }
          }
        }
    */
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/logout")
    /*
        #swagger.summary = '로그아웃'
        #swagger.description = '발급된 리프레시 토큰을 폐기하고 세션을 종료합니다.'
        #swagger.parameters['Authorization'] = {
          in: 'header',
          required: true,
          description: '현재 발급된 액세스 토큰 (Bearer {token})'
        }
        #swagger.requestBody = {
          required: false,
          content: {
            "application/json": {
              schema: {
                type: "object",
                properties: {
                  refreshToken: { type: "string", description: "폐기할 리프레시 토큰" }
                }
              }
            }
          }
        }
    */
    public ResponseEntity<LogoutRequest.Response> logout(@RequestHeader("Authorization") String authorization,
                                                         @RequestBody(required = false) LogoutRequest request) {
        return ResponseEntity.ok(authService.logout(authorization, request));
    }

    @PostMapping("/refresh")
    /*
        #swagger.summary = '토큰 재발급'
        #swagger.description = '만료된 액세스 토큰을 대체할 새 토큰 쌍을 발급합니다.'
        #swagger.requestBody = {
          required: true,
          content: {
            "application/json": {
              schema: {
                type: "object",
                properties: {
                  refreshToken: { type: "string", description: "유효한 리프레시 토큰" }
                },
                required: ["refreshToken"]
              }
            }
          }
        }
    */
    public ResponseEntity<AuthResponse> refresh(@RequestBody RefreshRequest request) {
        return ResponseEntity.ok(authService.refresh(request));
    }

    @PostMapping("/social/login")
    /*
        #swagger.summary = '소셜 로그인'
        #swagger.description = '카카오, 구글, 네이버 액세스 토큰을 이용해 로그인합니다.'
        #swagger.requestBody = {
          required: true,
          content: {
            "application/json": {
              schema: {
                type: "object",
                properties: {
                  provider: { type: "string", description: "로그인 제공자 (KAKAO/GOOGLE/NAVER)" },
                  accessToken: { type: "string", description: "소셜 액세스 토큰" },
                  idToken: { type: "string", description: "(선택) OpenID Connect ID 토큰" },
                  deviceInfo: {
                    type: "object",
                    description: "접속 기기 정보",
                    properties: {
                      os: { type: "string", description: "운영체제 이름" },
                      appVersion: { type: "string", description: "앱 버전" }
                    }
                  }
                },
                required: ["provider", "accessToken"]
              }
            }
          }
        }
    */
    public ResponseEntity<AuthResponse> socialLogin(@RequestBody SocialLoginRequest request) {
        return ResponseEntity.ok(authService.socialLogin(request));
    }
}