package com.example.demo.Controller;

import com.example.demo.Domain.Common.Dto.PsyReserveDto;
import com.example.demo.Domain.Common.Dto.SignupDto;
import com.example.demo.Domain.Common.Entity.Signup;
import com.example.demo.Domain.Common.Service.PsyReserveService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttribute;

@Controller
@Slf4j
@RequiredArgsConstructor
public class MypageController {

    private final PsyReserveService psyReserveService;

    /** 세션 로그인 사용자 + 예약 정보 로드 */
    @GetMapping("/mypage/Mypage")
    public String mypage(@SessionAttribute(value = "loginUser", required = false) Object loginUser,
                         @RequestParam(required = false) String email,
                         Model model) {

        //  세션 사용자 확인 (자체 로그인 / 소셜 로그인 둘 다 대응)
        String name = null;
        String userEmail = null;

        if (loginUser instanceof SignupDto user) { // 소셜 로그인 세션
            name = user.getName();
            userEmail = user.getOauthEmail();
            model.addAttribute("userType", "social");
            log.info("소셜 로그인 사용자: {} / 이메일: {}", name, userEmail);


        } else if (loginUser instanceof Signup user) { // 자체 로그인 세션
            name = user.getName();
            userEmail = user.getEmailId() + "@" + user.getEmailDomain();
            model.addAttribute("userType", "native");
            log.info("자체 로그인 사용자: {} / 이메일: {}", name, userEmail);

        } else {
            log.warn("세션에 로그인 정보가 없습니다. (비로그인 상태)");
            return "redirect:/signin"; // 세션 없으면 로그인 페이지로
        }

        model.addAttribute("user", name);
        model.addAttribute("email", userEmail);

        // 심리 예약 정보 조회
        if (email != null && !email.isEmpty()) {
            PsyReserveDto reserve = psyReserveService.findByEmail(email);
            if (reserve != null) {
                log.info("예약 정보 로드 완료: {}", reserve);
                model.addAttribute("reserve", reserve);
            } else {
                log.info("해당 이메일로 예약 정보 없음: {}", email);
            }
        } else {
            log.info("예약 조회용 이메일 파라미터가 전달되지 않음.");
        }

        return "mypage/Mypage";
    }
}
