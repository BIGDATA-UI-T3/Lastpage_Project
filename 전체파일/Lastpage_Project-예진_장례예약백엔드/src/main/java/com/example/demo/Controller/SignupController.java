package com.example.demo.Controller;

import com.example.demo.Domain.Common.Dto.MemberDto;
import com.example.demo.Domain.Common.Service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Controller
@RequiredArgsConstructor
@RequestMapping("/member")
public class SignupController {

    private final MemberService memberService;

    @PostMapping("/signup")
    @ResponseBody
    public ResponseEntity<?> signup(@RequestBody MemberDto memberDto) {
        try {
            memberService.signup(memberDto);
            return ResponseEntity.ok().body(Map.of("success", true));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage(),
                    "field", detectErrorField(e.getMessage())
            ));
        }
    }

    private String detectErrorField(String message) {
        if (message.contains("아이디")) return "username";
        if (message.contains("이메일")) return "email";
        if (message.contains("비밀번호")) return "password";
        if (message.contains("인증번호")) return "authCode";
        return "general";
    }

}
