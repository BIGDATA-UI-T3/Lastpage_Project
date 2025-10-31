package com.example.demo.Controller;

import com.example.demo.Domain.Common.Service.GoodsReserveService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller; // 👈 1. @Controller 인지 확인! (@RestController 아님)
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller // 👈 1. @Controller 인지 확인! (@RestController 아님)
@RequiredArgsConstructor
@RequestMapping("/goods")
public class GoodsReserveController {

    private final GoodsReserveService goodsReserveService;

    @PostMapping("/delete/{id}") // 👈 2. @PostMapping 인지 확인!
    public String deleteReservation(
            @PathVariable("id") Long id,
            RedirectAttributes redirectAttributes) {

        try {
            goodsReserveService.deleteReservation(id);
            redirectAttributes.addFlashAttribute("successMessage", "예약이 성공적으로 취소되었습니다.");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "예약 취소 중 오류가 발생했습니다: " + e.getMessage());
        }

        return "redirect:/mypage";
    }
}