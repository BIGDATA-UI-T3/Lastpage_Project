package com.example.demo.Controller;

import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Slf4j
@Controller
public class FooterController {

    /** footer.html을 세션 기반으로 렌더링 */
    @GetMapping("/html/footer.html")
    public String footer(Model model, HttpSession session) {
        Object loginUser = session.getAttribute("loginUser");

        model.addAttribute("isLoggedIn", loginUser != null);
        return "footer"; // templates/footer.html
    }
}
