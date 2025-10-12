package com.example.demo.controller;

import com.example.demo.domain.dtos.UserDto;
import com.example.demo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/login")
    public String showLoginPage() {
        return "login";
    }

    @PostMapping("/login")
    public String processLogin(@ModelAttribute UserDto userDto) {
        // [수정] 데이터가 Controller까지 잘 도착했는지 콘솔에 출력해서 확인합니다.
        System.out.println("컨트롤러가 받은 아이디: " + userDto.getUsername());
        System.out.println("컨트롤러가 받은 비밀번호: " + userDto.getPassword());

        userService.registerUser(userDto);
        return "redirect:/login-success";
    }

    @GetMapping("/login-success")
    public String showSuccessPage() {
        return "login_success";
    }
}