package com.example.demo.Config;

import com.example.demo.Domain.Common.Security.CustomUserPrincipal;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.ui.Model;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalControllerAdvice {

    @ModelAttribute
    public void addLoginUser(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal().equals("anonymousUser")) {
            return; // 로그인 안된 상태
        }

        Object principal = auth.getPrincipal();
        Map<String, Object> loginUser = new HashMap<>();

        if (principal instanceof CustomUserPrincipal customUser) {
            loginUser.put("ownerName", customUser.getMember().getName());
            loginUser.put("username", customUser.getUsername());
            loginUser.put("provider", customUser.getAttributes().getOrDefault("provider", "LOCAL"));
        } else if (principal instanceof OAuth2User oauth) {
            loginUser.put("ownerName", oauth.getAttributes().getOrDefault("ownerName",
                    oauth.getAttributes().getOrDefault("name", oauth.getName())));
            loginUser.put("provider", oauth.getAttributes().getOrDefault("provider", "OAUTH"));
        }

        model.addAttribute("loginUser", loginUser);
    }
}
