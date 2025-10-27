package com.example.demo.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String home() {
        return "mainpage"; // -> templates/mainpage.html
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
