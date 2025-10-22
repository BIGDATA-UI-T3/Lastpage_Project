package com.example.demo.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LoginController {

    @GetMapping("/signin")
    public String signinPage() {
        return "signin"; // ✅ templates/signin.html 을 찾음
    }
}
