package com.example.demo.Controller;

import com.example.demo.Domain.Common.Entity.Signup;
import com.example.demo.Domain.Common.Service.AuthService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@Slf4j
@RequiredArgsConstructor
public class SigninController {

    private final AuthService authService;

    /** 로그인 페이지 이동 */
    @GetMapping("/signin")
    public String signinPage() {
        log.info("로그인 페이지로 이동합니다.");
        return "signin/Signin";
    }

    /** 자체 로그인 처리 */
    @PostMapping("/loginProc")
    public String login(@RequestParam String id,
                        @RequestParam String password,
                        HttpSession session,
                        RedirectAttributes redirectAttributes) {
        try {
            Signup user = authService.authenticate(id, password);

            // 세션 저장 (mypage 접근용)
            session.setAttribute("loginUser", user);
            session.setAttribute("loginEmail", user.getEmailId());
            session.setAttribute("loginName", user.getName());
            log.info("[로그인 성공] Id={}, Email={}", user.getId(), user.getEmailId());

            //  로그인 성공 시 마이페이지로 이동
            return "redirect:/";

        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            log.warn("[로그인 실패] {}", e.getMessage());
            return "redirect:/signin";
        }
    }

    /** 로그아웃 처리 */
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        log.info("로그아웃 완료");
        return "redirect:/";
    }
}
