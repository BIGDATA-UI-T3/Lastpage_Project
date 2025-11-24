package com.example.demo.Domain.Common.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    // 회원가입 이메일 인증용 코드 저장소
    private final Map<String, String> codeStore = new HashMap<>();

    // ==========================================================
    // 1. 회원가입 이메일 인증번호 발송
    // ==========================================================
    public void sendAuthCode(String email) throws MessagingException {

        String code = String.valueOf(new Random().nextInt(900000) + 100000);
        codeStore.put(email, code);

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");

        helper.setTo(email);
        helper.setSubject("[Last Page] 이메일 인증번호");
        helper.setText("인증번호: " + code);

        mailSender.send(message);
    }

    // 인증번호 비교
    public boolean verifyCode(String email, String code) {
        return code.equals(codeStore.get(email));
    }

    // ==========================================================
    // 2. 비밀번호 찾기 - 임시 비밀번호 이메일 발송
    // ==========================================================
    public void sendTempPasswordMail(String email, String tempPw)
            throws MessagingException {

        MimeMessage msg = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(msg, true, "UTF-8");

        helper.setTo(email);
        helper.setSubject("[LastPage] 임시 비밀번호 안내");

        helper.setText("""
                안녕하세요, LastPage입니다.<br><br>
                요청하신 <b>임시 비밀번호</b>를 발급해드렸습니다.<br><br>
                임시 비밀번호: <b>%s</b><br><br>
                로그인 후 반드시 새 비밀번호로 변경해주세요.<br><br>
                감사합니다.
                """.formatted(tempPw), true);

        mailSender.send(msg);
    }
}
