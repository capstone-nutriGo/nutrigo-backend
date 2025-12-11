package com.nutrigo.nutrigo_backend.domain.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/login")
@RequiredArgsConstructor
public class LoginViewController {

    private final KakaoOauthService kakaoOauthService;

    @GetMapping("/kakao")
    public String kakaoLogin(Model model) {
        String kakaoLoginUrl = kakaoOauthService.buildAuthorizeUri("login-view");
        model.addAttribute("kakaoLoginUrl", kakaoLoginUrl);
        return "kakaoLogin";
    }
}