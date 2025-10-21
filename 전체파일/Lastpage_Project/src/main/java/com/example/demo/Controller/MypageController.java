package com.example.demo.Controller;

import com.example.demo.Domain.Common.Entity.PsyReserve;
import com.example.demo.Domain.Common.Service.PsyReserveService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@Slf4j
@RequiredArgsConstructor
public class MypageController {

    private final PsyReserveService psyReserveService;

    @GetMapping("/mypage/Mypage")
    public String mypage(@RequestParam(required = false) String email, Model model) {
        if (email != null && !email.isEmpty()) {
            PsyReserve reserve = psyReserveService.findByEmail(email);
            model.addAttribute("reserve", reserve);
            log.info("📄 MyPage 예약 정보 로드 완료: {}", reserve);
        } else {
            log.warn("⚠️ 이메일 파라미터가 비어있습니다.");
        }
        return "mypage/Mypage"; // templates/mypage/Mypage.html
    }
}
