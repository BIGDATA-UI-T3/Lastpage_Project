package com.example.demo.Controller;

import com.example.demo.Domain.Common.Dto.SignupDto;
import com.example.demo.Domain.Common.Entity.Signup;
import com.example.demo.Domain.Common.Service.SignupService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttribute;

@Controller
@Slf4j
@RequiredArgsConstructor
public class EditInfoController {

    private final SignupService signupService;

    @GetMapping("/mypage/EditInfo")
    public String EditInfo(@SessionAttribute(value = "loginUser", required = false) Object loginUser,
                           Model model) {

        //  세션 사용자 확인 (자체 로그인 / 소셜 로그인 둘 다 대응)
        String name = null;
        String userEmail = null;
        if (loginUser == null) {
            log.warn("비로그인 사용자의 접근 시도 → /signin 리다이렉트");
            return "redirect:/signin";
        }

        else if (loginUser instanceof SignupDto user) { // 소셜 로그인 세션
            name = user.getName();
            userEmail = user.getOauthEmail();
            model.addAttribute("userType", "social");
            log.info("소셜 로그인 사용자: {} / 이메일: {}", name, userEmail);


        } else if (loginUser instanceof Signup user) { // 자체 로그인 세션
            name = user.getName();
            userEmail = user.getEmailId() + "@" + user.getEmailDomain();
            model.addAttribute("userType", "native");
            log.info("자체 로그인 사용자: {} / 이메일: {}", name, userEmail);

        }

        log.info("회원정보 수정 페이지 접근 성공");
        return "mypage/EditInfo";
    }

}
