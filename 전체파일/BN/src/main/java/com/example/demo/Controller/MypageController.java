package com.example.demo.Controller;

import com.example.demo.Domain.Common.Dto.FuneralReserveDto;
import com.example.demo.Domain.Common.Dto.GoodsReserveDto;
import com.example.demo.Domain.Common.Dto.PsyReserveDto;
import com.example.demo.Domain.Common.Dto.SignupDto;
import com.example.demo.Domain.Common.Entity.GoodsReserve;
import com.example.demo.Domain.Common.Entity.Signup;
import com.example.demo.Domain.Common.Service.FuneralReserveService;
import com.example.demo.Domain.Common.Service.GoodsReserveService;
import com.example.demo.Domain.Common.Service.PsyReserveService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.SessionAttribute;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@Slf4j
@RequiredArgsConstructor
public class MypageController {

    private final PsyReserveService psyReserveService;
    private final GoodsReserveService goodsReserveService;
    private final FuneralReserveService funeralReserveService;

    /** 세션 로그인 사용자 + 예약 정보 로드 */
    @GetMapping("/mypage/Mypage")
    public String mypage(@SessionAttribute(value = "loginUser", required = false) Object loginUser,
                         Model model) {

        // 로그인 세션이 없을 경우
        if (loginUser == null) {
            log.warn("세션에 로그인 정보가 없습니다. (비로그인 상태)");
            return "redirect:/signin";
        }

        String name = null;
        String userSeq = null;
        String userType = null;

        // 소셜 로그인 세션 (SignupDto)
        if (loginUser instanceof SignupDto user) {
            name = user.getName();
            userSeq = user.getUserSeq(); // DTO에 추가된 UUID
            userType = "social";
            model.addAttribute("userType", userType);
            log.info("소셜 로그인 사용자: {} / user_seq={}", name, userSeq);
        }
        //자체 로그인 세션 (Signup)
        else if (loginUser instanceof Signup user) {
            name = user.getName();
            userSeq = user.getUserSeq();
            userType = "native";
            model.addAttribute("userType", userType);
            log.info("자체 로그인 사용자: {} / user_seq={}", name, userSeq);
        }

        // 공통 모델 데이터
        model.addAttribute("user", name);
        model.addAttribute("userSeq", userSeq);
        model.addAttribute("userType", userType);
        // 예약 정보 로드 (user_seq 기준)
        List<PsyReserveDto> psyReservationList = psyReserveService.findAllByUserSeq(userSeq);
        model.addAttribute("psyReservationList", psyReservationList);
        List<GoodsReserveDto> goodsReservationList = goodsReserveService.findAllByUserSeq(userSeq);
        model.addAttribute("goodsReservationList", goodsReservationList);
        List<FuneralReserveDto> funeralReservationList = funeralReserveService.findAllByUserSeq(userSeq);
        model.addAttribute("funeralReservationList", funeralReservationList);


        return "mypage/Mypage";
    }
}
