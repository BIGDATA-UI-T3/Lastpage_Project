package com.example.demo.Controller;


import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller

@Slf4j
public class HomeController {

    @GetMapping("/")//http://localhost:8099/
    public String home(){
        System.out.println("GET /");
        log.info("GET /....");
        return "mainpage/Mainpage";
    }
    @GetMapping({ "/index"})
    public String index() {
        return "index";
    }

    // 인사말
    @GetMapping("/aboutuspage/aboutus")
    public String aboutUsPage() {
        return "aboutuspage/aboutus";
    }

}
