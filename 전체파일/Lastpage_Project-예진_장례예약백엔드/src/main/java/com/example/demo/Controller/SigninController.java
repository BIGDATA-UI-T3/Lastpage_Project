package com.example.demo.Controller;

import com.example.demo.Domain.Common.Dto.MemberDto;
import com.example.demo.Domain.Common.Dto.SigninDto;
import com.example.demo.Domain.Common.Service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/member")
public class SigninController {

    private final MemberService memberService;

    @PostMapping("/signin")
    public Map<String, Object> signin(@RequestBody MemberDto dto) {
        try {
            MemberDto loginMember = memberService.signin(dto);
            return Map.of(
                    "success", true,
                    "redirect", "/"
            );
        } catch (IllegalArgumentException e) {
            return Map.of(
                    "success", false,
                    "message", e.getMessage()
            );
        }
    }
}
