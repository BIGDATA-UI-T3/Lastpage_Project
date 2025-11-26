package com.example.demo.Controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@Slf4j
public class GoodsServiceController {
    @GetMapping("/goodsService") //주소창 이름입니다!!
    public String goodspage(){
        System.out.println("굿즈서비스 메인페이지입니다.");
        log.info("굿즈서비스 메인페이지입니다.");
        return "goodspage/Goods";
    }
}
