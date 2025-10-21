package com.example.demo.Controller; // (대문자 C 패키지 사용 시)

import com.example.demo.Domain.Common.Entity.GoodsReserve;
import com.example.demo.Domain.Common.Service.GoodsReserveService;
import com.example.demo.Domain.Common.Dto.UserDto;
import com.example.demo.Domain.Common.Service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

@Controller
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private GoodsReserveService goodsReserveService;

    // "로그인 페이지" 보여주기
    @GetMapping("/login")
    public String showLoginPage() {
        return "login";
    }

    // "로그인" (사실상 회원가입) 정보 DB에 저장하기
    @PostMapping("/register")
    public String processRegister(@ModelAttribute UserDto userDto) {
        System.out.println("--- UserController 도착! ---");
        System.out.println("컨트롤러가 받은 아이디: " + userDto.getUsername());
        System.out.println("컨트롤러가 받은 비밀번호: " + userDto.getPassword());

        userService.registerUser(userDto);
        return "redirect:/login-success";
    }

    // 성공 페이지 보여주기
    @GetMapping("/login-success")
    public String showSuccessPage() {
        return "login_success";
    }

    // "마이페이지" 보여주기
    @GetMapping("/mypage")
    public String showMyPage(Model model) {
        // 1. 서비스에게 모든 굿즈 예약 목록을 가져오라고 시킴
        List<GoodsReserve> goodsList = goodsReserveService.getAllGoodsReservations();

        // 2. Model에 담아서 HTML로 전달
        model.addAttribute("goodsReservationList", goodsList);

        return "mypage";
    }
}