package com.example.demo.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class FindViewController {

    @GetMapping("/find/id")
    public String viewFindId() {
        return "signin/FindId";
    }

    @GetMapping("/find/password")
    public String viewFindPw() {
        return "signin/FindPassword";
    }
}
