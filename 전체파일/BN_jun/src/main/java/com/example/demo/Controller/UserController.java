package com.example.demo.Controller; // (대문자 C 패키지 사용 시)

import com.example.demo.Domain.Common.Dto.RegisterFormDto;
import com.example.demo.Domain.Common.Entity.GoodsReserve;
import com.example.demo.Domain.Common.Service.GoodsReserveService;
import com.example.demo.Domain.Common.Service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

@Slf4j
@Controller
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private GoodsReserveService goodsReserveService;

    // "회원가입 페이지" 보여주기 (이건 정상)
    @GetMapping("/signup")
    public String showRegisterPage() {
        return "signup"; // templates/signup.html
    }

    // "회원가입" 데이터 처리 (이건 정상)
    @PostMapping("/signup")
    public String processRegister(@ModelAttribute RegisterFormDto dto) {

        try {
            userService.registerUser(dto);
        } catch (Exception e) {
            log.info("오류?");
            return "redirect:/signup?error=" + e.getMessage();
        }

        // 회원가입 성공 시 로그인 페이지로 이동
        return "redirect:/signin";
    }

    // "마이페이지" 보여주기 (이건 정상)
    @GetMapping("/mypage")
    public String showMyPage(Model model) {
        List<GoodsReserve> goodsList = goodsReserveService.getAllGoodsReservations();
        model.addAttribute("goodsReservationList", goodsList);
        return "mypage";
    }
}