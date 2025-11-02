package com.example.demo.Controller;

import com.example.demo.Domain.Common.Dto.ReserveDto; // [추가]
import com.example.demo.Domain.Common.Entity.GoodsReserve; // [추가]
import com.example.demo.Domain.Common.Service.GoodsReserveService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity; // [추가]
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model; // [추가]
import org.springframework.web.bind.annotation.*; // [수정]
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal; // [추가]

@Controller
@RequiredArgsConstructor
@RequestMapping("/goods")
public class GoodsReserveController {

    private final GoodsReserveService goodsReserveService;

    @GetMapping("/edit/{id}")
    public String showEditPage(@PathVariable("id") Long id, Model model, Principal principal) {

        GoodsReserve existingData = goodsReserveService.getReservationById(id);

        if (principal == null || !existingData.getUser().getUsername().equals(principal.getName())) {
            // TODO: 권한 없음 오류 페이지로 보내면 더 좋습니다.
            return "redirect:/mypage";
        }

        model.addAttribute("existingData", existingData);

        return "reserve/Goods_reserve";
    }

    @PostMapping("/update/{id}")
    @ResponseBody
    public ResponseEntity<?> updateReservation(
            @PathVariable("id") Long id,
            @RequestBody ReserveDto dto,
            Principal principal) {

        if (principal == null) {
            return ResponseEntity.status(401).body("로그인이 필요합니다.");
        }

        try {
            String username = principal.getName();
            GoodsReserve updated = goodsReserveService.updateReservation(id, dto, username);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("예약 수정 실패: " + e.getMessage());
        }
    }

    @PostMapping("/delete/{id}")
    public String deleteReservation(
            @PathVariable("id") Long id,
            RedirectAttributes redirectAttributes) {

        try {
            // TODO: [보안 강화] 삭제하기 전에도 principal을 받아 본인 확인 로직이 필요합니다.
            goodsReserveService.deleteReservation(id);
            redirectAttributes.addFlashAttribute("successMessage", "예약이 성공적으로 취소되었습니다.");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "예약 취소 중 오류가 발생했습니다: " + e.getMessage());
        }

        return "redirect:/mypage";
    }
}