package com.example.demo.Domain.Common.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value; // 👈 [추가]
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
@RequiredArgsConstructor
public class MailService {

    private final JavaMailSender javaMailSender;

    // application.properties의 username을 가져오기
    @Value("${spring.mail.username}")
    private String fromEmail;

    // 6자리 인증 코드 생성
    public String createVerificationCode() {
        Random random = new Random();
        int code = 100000 + random.nextInt(900000); // 100000 ~ 999999
        return String.valueOf(code);
    }

    // 이메일 전송
    public String sendVerificationEmail(String email) throws MessagingException {
        String code = createVerificationCode();

        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");

        // 이메일 템플릿 (HTML)
        String htmlContent = "<div style='font-family: Arial, sans-serif; text-align: center; padding: 20px; border: 1px solid #ddd; border-radius: 10px;'>"
                + "<h2>Lastpage 회원가입 인증 코드</h2>"
                + "<p>저희 서비스를 이용해주셔서 감사합니다. 인증 코드 6자리를 입력해주세요.</p>"
                + "<div style='font-size: 28px; font-weight: bold; background-color: #f5f5f5; padding: 15px; border-radius: 5px; margin: 20px 0;'>"
                + code
                + "</div>"
                + "<p style='font-size: 12px; color: #888;'>이 메일은 발신 전용입니다.</p>"
                + "</div>";

        helper.setTo(email); // 수신자
        helper.setSubject("[Lastpage] 회원가입 인증 코드입니다.");
        helper.setText(htmlContent, true);

        // 3단계에서 설정한 발신자 이메일 (username)
        helper.setFrom(fromEmail); // 발신자(고정값임)

        javaMailSender.send(mimeMessage);

        return code; // 컨트롤러에게 인증 코드 반환
    }
}