package com.example.demo.Controller;

import com.example.demo.Domain.Common.Entity.FuneralReserve;
import com.example.demo.Domain.Common.Service.FuneralReserveService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class MypageController {

    private final FuneralReserveService reserveService;

//    @GetMapping("/mypage")
//    public String myPage(@RequestParam("ownerName") String ownerName, Model model) {
//        List<FuneralReserve> reserves = reserveService.getReserveByOwnerName(ownerName);
//        model.addAttribute("reserves", reserves);
//        model.addAttribute("ownerName", ownerName);
//        return "mypage";
//    }

    @GetMapping("/mypage")
    public String myPage(@RequestParam("ownerName") String ownerName, Model model) {
        System.out.println("========== /mypage 요청 ===========");
        System.out.println("ownerName 파라미터 값: " + ownerName);  // ← 여기가 핵심
        List<FuneralReserve> reserves = reserveService.getReserveByOwnerName(ownerName);
        System.out.println("조회된 예약 건수: " + reserves.size());   // ← 실제 DB에서 가져온 사이즈 확인
        model.addAttribute("reserves", reserves);
        model.addAttribute("ownerName", ownerName);
        return "mypage";
    }

}
