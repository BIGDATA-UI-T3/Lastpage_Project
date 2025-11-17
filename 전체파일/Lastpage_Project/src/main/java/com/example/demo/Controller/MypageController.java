package com.example.demo.Controller;

import com.example.demo.Domain.Common.Dto.FuneralReserveDto;
import com.example.demo.Domain.Common.Dto.GoodsReserveDto;
import com.example.demo.Domain.Common.Dto.PsyReserveDto;
import com.example.demo.Domain.Common.Dto.SignupDto;
import com.example.demo.Domain.Common.Entity.Signup;
import com.example.demo.Domain.Common.Service.FuneralReserveService;
import com.example.demo.Domain.Common.Service.GoodsReserveService;
import com.example.demo.Domain.Common.Service.PsyReserveService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.SessionAttribute;

import java.util.List;

@Controller
@Slf4j
@RequiredArgsConstructor
public class MypageController {

    private final PsyReserveService psyReserveService;
    private final GoodsReserveService goodsReserveService;
    private final FuneralReserveService funeralReserveService;

    @GetMapping("/mypage/Mypage")
    public String mypage(
            @SessionAttribute(value = "loginUser", required = false) Object loginUser,
            HttpSession session,
            Model model) {

        /* ============================================
         * 1) Security 인증 정보 확인 (세션 없을 경우 대비)
         * ============================================ */
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (loginUser == null && auth != null && auth.isAuthenticated()
                && !"anonymousUser".equals(auth.getPrincipal())) {

            // Security 인증된 사용자 → 세션에 다시 기록
            String userSeq = auth.getName(); // principal = userSeq
            log.info("Security 인증 사용자 → 세션 자동 등록 userSeq={}", userSeq);

            // 기존 SignupDto/Signup 구조가 있으므로 최소 정보만 세션 등록
            SignupDto dto = new SignupDto();
            dto.setUserSeq(userSeq);
            dto.setName("사용자"); // 필요 시 DB 조회로 이름 가져오도록 개선 가능

            session.setAttribute("loginUser", dto);
            loginUser = dto; // 아래 공통 처리로 이어지도록 덮어쓰기
        }

        /* ============================================
         * 2) 그래도 세션이 없다면 비로그인 → 리디렉트
         * ============================================ */
        if (loginUser == null) {
            log.warn("로그인 정보 없음 → 로그인 페이지로 이동");
            return "redirect:/signin";
        }

        /* ============================================
         * 3) 유저 정보 파싱 (Signup or SignupDto)
         * ============================================ */
        String name = null;
        String userSeq = null;
        String userType = null;

        if (loginUser instanceof SignupDto user) {
            name = user.getName();
            userSeq = user.getUserSeq();
            userType = "social";
            model.addAttribute("userType", "social");

            log.info("소셜 로그인 사용자 → {} / userSeq={}", name, userSeq);

        } else if (loginUser instanceof Signup user) {
            name = user.getName();
            userSeq = user.getUserSeq();
            userType = "native";
            model.addAttribute("userType", "native");

            log.info("자체 로그인 사용자 → {} / userSeq={}", name, userSeq);
        }

        /* ============================================
         * 4) 공통 데이터 모델 등록
         * ============================================ */
        model.addAttribute("user", name);
        model.addAttribute("userSeq", userSeq);
        model.addAttribute("userType", userType);

        // 예약 정보 로드
        model.addAttribute("psyReservationList", psyReserveService.findAllByUserSeq(userSeq));
        model.addAttribute("goodsReservationList", goodsReserveService.findAllByUserSeq(userSeq));
        model.addAttribute("funeralReservationList", funeralReserveService.findAllByUserSeq(userSeq));

        return "mypage/Mypage";
    }
}
