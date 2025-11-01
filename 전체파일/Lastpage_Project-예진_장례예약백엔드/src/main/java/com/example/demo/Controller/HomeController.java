package com.example.demo.Controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String home(@AuthenticationPrincipal OAuth2User oAuth2User,
                       Authentication authentication,
                       Model model, HttpSession session) {

        Object loginUser = session.getAttribute("loginUser");

        if (authentication != null && authentication.isAuthenticated()) {
            // OAuth2 로그인 사용자
            if (oAuth2User != null) {
                System.out.println("로그인 사용자 정보: " + oAuth2User.getAttributes());
                model.addAttribute("loginUser", oAuth2User.getAttributes());
            } else {
                // 일반 로그인 사용자(폼 로그인 등)
                System.out.println("로그인 사용자 정보: " + authentication.getName());
                model.addAttribute("loginUser", authentication.getName());
            }
        } else {
            // 로그인 시도 -> 실패했을 때 출력
            if (authentication != null) {
                System.out.println("로그인 실패");
            }
            model.addAttribute("loginUser", null); // 로그인 안됨
        }

        return "mainpage"; // 메인페이지 HTML 이름
    }

    @GetMapping("/signin")
    public String signinPage() {
        return "signin"; // -> templates/login.html
    }

    @GetMapping("/signup")
    public String signupPage() {
        return "signup"; // -> templates/signup.html
    }
}
