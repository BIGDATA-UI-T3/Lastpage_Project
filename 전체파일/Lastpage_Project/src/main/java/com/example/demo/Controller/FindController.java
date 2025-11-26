package com.example.demo.Controller;

import com.example.demo.Domain.Common.Dto.FindIdRequestDto;
import com.example.demo.Domain.Common.Dto.FindIdResponseDto;
import com.example.demo.Domain.Common.Dto.FindPasswordRequestDto;
import com.example.demo.Domain.Common.Dto.FindPasswordResponseDto;
import com.example.demo.Domain.Common.Entity.Signup;
import com.example.demo.Domain.Common.Service.EmailService;
import com.example.demo.Repository.SignupRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.security.SecureRandom;

@RestController
@RequestMapping("/api/find")
@RequiredArgsConstructor
public class FindController {

    private final SignupRepository signupRepository;
    private final EmailService emailService;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    // ================================
    // 1. 아이디 찾기
    // ================================
    @PostMapping("/id")
    public FindIdResponseDto findId(@RequestBody FindIdRequestDto req) {

        String name = req.getName();
        String emailId = req.getEmailId();
        String emailDomain = req.getEmailDomain();

        if (name == null || emailId == null || emailDomain == null
                || name.isBlank() || emailId.isBlank() || emailDomain.isBlank()) {
            return new FindIdResponseDto("fail", "이름과 이메일을 모두 입력해주세요.");
        }

        // 소셜 계정(provider != null)은 제외
        Signup user = signupRepository
                .findByNameAndEmailIdAndEmailDomainAndProviderIsNull(name, emailId, emailDomain)
                .orElse(null);

        if (user == null) {
            return new FindIdResponseDto("fail", "일치하는 회원 정보가 없습니다.");
        }

        // 아이디 일부 마스킹
        String maskedId = maskUserId(user.getId());

        return new FindIdResponseDto("ok", maskedId);
    }

    // 아이디 마스킹 (예: abcd → ab**)
    private String maskUserId(String id) {
        if (id == null || id.length() <= 2) {
            return "*".repeat(id.length());
        }
        return id.substring(0, 2) + "*".repeat(id.length() - 2);
    }


    // ================================
    // 2. 비밀번호 재발급
    // ================================
    @PostMapping("/password")
    public FindPasswordResponseDto findPassword(@RequestBody FindPasswordRequestDto req) {

        String id = req.getId();
        String emailId = req.getEmailId();
        String emailDomain = req.getEmailDomain();

        if (id == null || emailId == null || emailDomain == null
                || id.isBlank() || emailId.isBlank() || emailDomain.isBlank()) {
            return new FindPasswordResponseDto("fail", "아이디와 이메일을 모두 입력해주세요.");
        }

        // 소셜 계정(provider != null)은 제외
        Signup user = signupRepository
                .findByIdAndEmailIdAndEmailDomainAndProviderIsNull(id, emailId, emailDomain)
                .orElse(null);

        if (user == null) {
            return new FindPasswordResponseDto("fail", "일치하는 회원 정보가 없습니다.");
        }

        // 1) 임시 비밀번호 생성
        String tempPw = generateTempPassword();

        // 2) 암호화 저장
        user.setPassword(passwordEncoder.encode(tempPw));
        signupRepository.save(user);

        // 3) 이메일 전송
        try {
            String fullEmail = emailId + "@" + emailDomain;
            emailService.sendTempPasswordMail(fullEmail, tempPw);
        } catch (Exception e) {
            e.printStackTrace();
            return new FindPasswordResponseDto("fail", "임시 비밀번호 이메일 전송에 실패했습니다.");
        }

        return new FindPasswordResponseDto("ok", "임시 비밀번호가 이메일로 발송되었습니다.");
    }


    // ================================
    // 임시 비밀번호 생성
    // ================================
    private String generateTempPassword() {
        final String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        SecureRandom rnd = new SecureRandom();
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < 10; i++) {
            sb.append(chars.charAt(rnd.nextInt(chars.length())));
        }
        return sb.toString();
    }
}
