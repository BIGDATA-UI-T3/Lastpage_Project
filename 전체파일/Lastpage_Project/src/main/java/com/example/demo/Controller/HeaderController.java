package com.example.demo.Controller;

import com.example.demo.Domain.Common.Dto.SignupDto;
import com.example.demo.Domain.Common.Entity.Signup;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Slf4j
@Controller
public class HeaderController {

    /** ------------------------------
     *  [공용 헤더 로드 컨트롤러]
     *  - templates/html/header.html 렌더링
     *  - 세션에 loginUser(Signup 또는 SignupDto)가 존재하면 로그인 상태 전달
     * ------------------------------ */
    @GetMapping("/html/header.html")
    public String header(Model model, HttpSession session) {

        Object loginUser = session.getAttribute("loginUser");

        if (loginUser != null) {
            String username = null;

            // 일반 회원 (Signup)
            if (loginUser instanceof Signup user) {
                username = user.getName();
                log.debug("[HeaderController] 로그인 사용자 감지: {}", username);
            }
            // 소셜 회원 (SignupDto)
            else if (loginUser instanceof SignupDto dto) {
                username = dto.getName();
                log.debug("[HeaderController] 소셜 로그인 사용자 감지: {}", username);
            }

            model.addAttribute("isLoggedIn", true);
            model.addAttribute("username", username);
        } else {
            model.addAttribute("isLoggedIn", false);
            model.addAttribute("username", "");
            log.debug("[HeaderController] 비로그인 상태로 헤더 렌더링");
        }

        return "header"; // templates/html/header.html
    }
}
