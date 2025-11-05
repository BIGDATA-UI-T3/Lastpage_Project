package com.example.demo.Controller;

import com.example.demo.Domain.Common.Dto.ReserveDto;
import com.example.demo.Domain.Common.Entity.FuneralReserve;
import com.example.demo.Domain.Common.Service.FuneralReserveService;
import com.example.demo.Domain.Common.Entity.Member;
import com.example.demo.Domain.Common.Security.CustomUserPrincipal;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class MypageController {

    private final FuneralReserveService reserveService;

    @GetMapping("/mypage")
    public String mypage(@AuthenticationPrincipal CustomUserPrincipal userPrincipal,
                         @AuthenticationPrincipal OAuth2User oAuth2User,
                         Authentication authentication,
                         HttpSession session,
                         Model model) {

        Member loginMember = (Member) session.getAttribute("loginUser");


        // 자체 로그인
//        if (loginMember != null) {
//            model.addAttribute("loginUser", loginMember.getUsername());
//        }
//        // OAuth2 로그인
//        else if (authentication != null && oAuth2User != null) {
//            model.addAttribute("loginUser", oAuth2User.getAttributes());
//        } else if (authentication != null) {
//            model.addAttribute("loginUser", authentication.getName());
//        } else {
//            return "redirect:/signin"; // 로그인 안 된 경우
//        }

        if (loginMember != null) {
            model.addAttribute("loginUser", loginMember.getUsername());
        }

        // OAuth2 로그인 사용자 (카카오 or 네이버)
        else if (authentication != null && oAuth2User != null) {
            Map<String, Object> attrs = oAuth2User.getAttributes();

            // 네이버 로그인인 경우 (response 안에 정보가 들어있음)
            if (attrs.get("response") instanceof Map) {
                Map<String, Object> response = (Map<String, Object>) attrs.get("response");
                model.addAttribute("loginUser", response.get("name") + " (" + response.get("email") + ")");
            }
            // 그 외 (카카오 등 다른 OAuth2)
            else {
                model.addAttribute("loginUser", attrs);
            }
        }

        // 일반 SecurityContext 로그인 (예: username)
        else if (authentication != null) {
            model.addAttribute("loginUser", authentication.getName());
        }

        // 로그인 실패
        else {
            return "redirect:/signin";
        }

        // 로그인한 사용자의 예약 목록 가져오기
        String username = userPrincipal != null ? userPrincipal.getUsername() : null;
        if (username != null) {
            List<FuneralReserve> reserves = reserveService.getReservesByUsername(username);
            model.addAttribute("reserves", reserves);
        }

        return "mypage";
    }

}
