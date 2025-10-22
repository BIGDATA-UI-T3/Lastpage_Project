package com.example.demo.Controller;


import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller

@Slf4j
public class SigninController {

    @GetMapping("/signin")//http://localhost:8099/signin
    public String home(){
        System.out.println("GET /");
        log.info("GET /....");
        return "signin/Signin";
    }


}
