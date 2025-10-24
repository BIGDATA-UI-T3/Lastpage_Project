package com.example.demo.Controller;

import com.example.demo.Domain.Common.Service.MailService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class MailApiController {

    private final MailService mailService;
    private final HttpSession httpSession;

    @PostMapping("/signup/send-email-code")
    public ResponseEntity<Map<String, String>> sendEmailCode(
            @RequestParam("emailId") String emailId,
            @RequestParam("emailDomain") String emailDomain) {

        Map<String, String> response = new HashMap<>();
        String fullEmail = emailId + "@" + emailDomain;

        try {
            // MailService를 호출하여 이메일 발송
            String verificationCode = mailService.sendVerificationEmail(fullEmail);

            // 세션에 인증 코드 저장
            httpSession.setAttribute("emailVerificationCode", verificationCode);
            httpSession.setAttribute("emailForVerification", fullEmail);
            httpSession.setMaxInactiveInterval(180); // 180초

            response.put("message", "인증 코드가 발송되었습니다. 3분 이내에 입력해주세요.");
            return ResponseEntity.ok(response);

        } catch (Exception e) {

            response.put("message", "이메일 발송에 실패했습니다: " + e.getMessage());
            // 500 Internal Server Error 응답을 반환
            return ResponseEntity.status(500).body(response);
        }
    }
}