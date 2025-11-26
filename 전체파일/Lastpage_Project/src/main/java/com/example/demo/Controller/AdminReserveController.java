package com.example.demo.Controller;

import com.example.demo.Domain.Common.Service.Admin.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/admin/reserves")
public class AdminReserveController {

    private final AdminFuneralReserveService adminFuneralService;
    private final AdminGoodsReserveService adminGoodsService;
    private final AdminPsyReserveService adminPsyService;

    /** 전체 예약 리스트 페이지 */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public String list(Model model) {

        model.addAttribute("funeralList", adminFuneralService.getAllFuneralReserves());
        model.addAttribute("goodsList", adminGoodsService.getAllGoodsReserves());
        model.addAttribute("psyList", adminPsyService.getAllPsyReserves());

        return "admin/AdminReserveList";
    }

    /** 단건 상세 조회 */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{type}/{id}")
    public String detail(@PathVariable String type,
                         @PathVariable Long id,
                         Model model) {

        switch (type) {
            case "funeral" -> model.addAttribute("reserve", adminFuneralService.getFuneralReserveById(id));
            case "goods" -> model.addAttribute("reserve", adminGoodsService.getGoodsReserveById(id));
            case "psy" -> model.addAttribute("reserve", adminPsyService.getPsyReserveById(id));
            default -> throw new IllegalArgumentException("잘못된 타입입니다: " + type);
        }

        model.addAttribute("type", type);
        return "admin/AdminReserveDetail";
    }

//    /** 삭제 */
//    @PreAuthorize("hasRole('ADMIN')")
//    @GetMapping("/delete/{type}/{id}")
//    public String delete(@PathVariable String type,
//                         @PathVariable Long id) {
//
//        switch (type) {
//            case "funeral" -> adminFuneralService.deleteById(id);
//            case "goods" -> adminGoodsService.deleteById(id);
//            case "psy" -> adminPsyService.deleteById(id);
//            default -> throw new IllegalArgumentException("잘못된 타입입니다: " + type);
//        }
//
//        return "redirect:/admin/reserves";
//    }
}
