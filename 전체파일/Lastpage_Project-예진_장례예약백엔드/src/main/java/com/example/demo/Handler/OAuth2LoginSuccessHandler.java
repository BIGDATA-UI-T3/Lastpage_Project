package com.example.demo.Handler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    public OAuth2LoginSuccessHandler() {
        setDefaultTargetUrl("/"); // 로그인 성공 후 메인페이지
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        OAuth2User oauthUser = (OAuth2User) authentication.getPrincipal();
        Map<String, Object> attrs = oauthUser.getAttributes();

        // null-safe 처리
        String id = (String) attrs.get("id");
        if (id == null || id.isEmpty()) {
            id = "USER_" + System.currentTimeMillis();
        }

        String email = (String) attrs.get("email");
        if (email == null) email = "";

        String name = (String) attrs.get("ownerName");
        if (name == null) name = "";

        String provider = (String) attrs.get("provider");
        if (provider == null) provider = "UNKNOWN";

        // 세션에 LOGIN_USER 저장
        Map<String, Object> loginUser = new HashMap<>();
        loginUser.put("provider", provider);
        loginUser.put("id", id);
        loginUser.put("email", email);
        loginUser.put("name", name);

        HttpSession session = request.getSession(true);
        session.setAttribute("LOGIN_USER", loginUser);

        super.onAuthenticationSuccess(request, response, authentication);
    }
}
