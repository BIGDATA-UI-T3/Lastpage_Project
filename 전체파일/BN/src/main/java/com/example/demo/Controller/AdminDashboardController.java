package com.example.demo.Controller;

import com.example.demo.Domain.Common.Service.Admin.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/admin")
public class AdminDashboardController {

    private final AdminUserService adminUserService;
    private final AdminQnaService adminQnaService;
    private final AdminPsyReserveService adminPsyReserveService;
    private final AdminGoodsReserveService adminGoodsReserveService;
    private final AdminFuneralReserveService adminFuneralReserveService;


    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        log.info("[ADMIN 접근] AUTH = {}", auth);
        log.info("[ADMIN 접근] AUTHORITIES = {}", auth.getAuthorities());


        //  통계
        long userCount = adminUserService.countUsers();
        long qnaCount = adminQnaService.countQna();
        long psyCount = adminPsyReserveService.countPsyReserves();
        long goodsCount = adminGoodsReserveService.countGoodsReserves();
        long funeralCount = adminFuneralReserveService.countFuneralReserves();

        model.addAttribute("userCount", userCount);
        model.addAttribute("qnaCount", qnaCount);
        model.addAttribute("psyCount", psyCount);
        model.addAttribute("goodsCount", goodsCount);
        model.addAttribute("funeralCount", funeralCount);

        //  최신 데이터 5개
        model.addAttribute("recentUsers", adminUserService.findRecentUsers(5));
        model.addAttribute("recentQna", adminQnaService.findRecentQna(5));
        model.addAttribute("recentPsy", adminPsyReserveService.getRecentPsyReserves(5));
        model.addAttribute("recentGoods", adminGoodsReserveService.getRecentGoodsReserves(5));
        model.addAttribute("recentFuneral", adminFuneralReserveService.getRecentFuneralReserves(5));

        return "admin/AdminDashboard";
    }
}
