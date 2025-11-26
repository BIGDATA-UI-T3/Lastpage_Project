package com.example.demo.Domain.Common.Service;

import com.example.demo.Domain.Common.Dto.FindIdRequestDto;
import com.example.demo.Domain.Common.Dto.FindIdResponseDto;
import com.example.demo.Domain.Common.Dto.FindPasswordRequestDto;
import com.example.demo.Domain.Common.Dto.FindPasswordResponseDto;
import com.example.demo.Domain.Common.Entity.Signup;
import com.example.demo.Domain.Common.Service.EmailService;
import com.example.demo.Repository.SignupRepository;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;

@Service
@RequiredArgsConstructor
@Slf4j
public class FindService {

    private final SignupRepository signupRepository;
    private final EmailService emailService;

    // 아이디 찾기
    public FindIdResponseDto findId(FindIdRequestDto req) {
        Signup user = signupRepository
                .findByNameAndEmailIdAndEmailDomainAndProviderIsNull(
                        req.getName(),
                        req.getEmailId(),
                        req.getEmailDomain()
                )
                .orElse(null);

        if (user == null) {
            return new FindIdResponseDto("fail",  "일치하는 회원 정보가 없습니다.");
        }

        return new FindIdResponseDto("ok", user.getId());
    }

    // 비밀번호 찾기
    public FindPasswordResponseDto findPassword(FindPasswordRequestDto req) throws MessagingException {

        Signup user = signupRepository.findByIdAndEmailIdAndEmailDomainAndProviderIsNull(
                req.getId(),
                req.getEmailId(),
                req.getEmailDomain()
        ).orElse(null);

        if (user == null) {
            return new FindPasswordResponseDto("fail", "일치하는 회원 정보가 없습니다.");
        }

        String tempPw = generateTempPassword();
        user.setPassword(tempPw);
        signupRepository.save(user);

        // 이메일 발송
        emailService.sendTempPasswordMail(req.getEmailId() + "@" + req.getEmailDomain(), tempPw);

        return new FindPasswordResponseDto("ok", "임시 비밀번호가 이메일로 발송되었습니다.");
    }

    // 임시 비밀번호 생성
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
