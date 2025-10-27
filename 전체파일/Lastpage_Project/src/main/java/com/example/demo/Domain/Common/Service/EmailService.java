package com.example.demo.Domain.Common.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
@RequiredArgsConstructor
public class EmailService {
    private final JavaMailSender mailSender;
    private final Map<String, String> codeStore = new HashMap<>();

    public void sendAuthCode(String email) throws MessagingException {
        String code = String.valueOf(new Random().nextInt(900000) + 100000);
        codeStore.put(email, code);

        MimeMessage message = mailSender.createMimeMessage();
        message.setSubject("[Last Page] 이메일 인증번호");
        message.setText("인증번호: " + code);
        message.addRecipients(MimeMessage.RecipientType.TO, email);
        mailSender.send(message);
    }

    public boolean verifyCode(String email, String code) {
        return code.equals(codeStore.get(email));
    }
}
