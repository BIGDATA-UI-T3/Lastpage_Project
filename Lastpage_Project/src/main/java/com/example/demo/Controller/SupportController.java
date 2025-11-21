package com.example.demo.Controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@Slf4j
public class SupportController {
    @GetMapping("/supportService") //주소창 이름입니다!!
    public String supportpage(){
        System.out.println("고객센터 메인페이지입니다.");
        log.info("고객센터 메인페이지입니다.");
        return "support/Support";
    }
}
