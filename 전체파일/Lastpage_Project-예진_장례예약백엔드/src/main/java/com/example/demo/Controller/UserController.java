package com.example.demo.Controller;

import com.example.demo.Domain.Common.Dto.UserDto;
import com.example.demo.Domain.Common.Service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * http://localhost:8090/signin 주소로 GET 요청이 오면
     * templates 폴더의 login.html 파일을 찾아서 보여주는 역할
     */
    @GetMapping("/login")
    public String showLoginPage() {
        return "login";
    }

    /**
     * login.html의 form 태그가 action="/register"로 데이터를 보냈을 때
     * 그 데이터를 받아서 처리하는 역할
     */
    @PostMapping("/register")
    public String processRegister(@ModelAttribute UserDto userDto) {
        // 데이터가 Controller까지 잘 도착했는지 콘솔에 출력해서 확인
        System.out.println("--- UserController 도착! ---");
        System.out.println("컨트롤러가 받은 아이디: " + userDto.getUsername());
        System.out.println("컨트롤러가 받은 비밀번호: " + userDto.getPassword());

        // Service 계층에 데이터 처리를 위임
        userService.registerUser(userDto);

        // 수정 필요 - 로그인 이후 가는 페이지
        return "redirect:/login-success";
    }

    /**
     * DB 저장 성공 후 /login-success 주소로 이동했을 때
     * templates 폴더의 login_success.html 파일을 보여주는 역할
     */
    @GetMapping("/login-success")
    public String showSuccessPage() {
        return "login_success";
    }
}