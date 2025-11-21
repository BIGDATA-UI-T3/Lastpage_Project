package com.example.demo.Controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@Slf4j
public class PsyServiceController {
    @GetMapping("/psyService") //주소창 이름입니다!!
    public String psypage(){
        System.out.println("심리서비스 메인페이지 입니다.");
        log.info("심리서비스 메인페이지 입니다.");
        return "psypage/Psypage";
    }

}
