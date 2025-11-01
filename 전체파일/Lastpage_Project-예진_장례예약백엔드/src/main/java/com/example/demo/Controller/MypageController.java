package com.example.demo.Controller;

import com.example.demo.Domain.Common.Entity.Member;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MypageController {

    @GetMapping("/mypage")
    public String mypage(@AuthenticationPrincipal OAuth2User oAuth2User,
                         Authentication authentication,
                         HttpSession session,
                         Model model) {

        Member loginMember = (Member) session.getAttribute("loginUser");

        if (loginMember != null) {
            model.addAttribute("loginUser", loginMember.getUsername());
        } else if (authentication != null && oAuth2User != null) {
            model.addAttribute("loginUser", oAuth2User.getAttributes());
        } else if (authentication != null) {
            model.addAttribute("loginUser", authentication.getName());
        } else {
            return "redirect:/signin"; // 로그인 안 된 경우
        }

        return "mypage";
    }

}
