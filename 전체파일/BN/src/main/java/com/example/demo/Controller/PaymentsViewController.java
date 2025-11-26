package com.example.demo.Controller;

import com.example.demo.Domain.Common.Entity.GoodsReserve;
import com.example.demo.Domain.Common.Entity.Payments;
import com.example.demo.Domain.Common.Service.GoodsReserveService;
import com.example.demo.Repository.GoodsReserveRepository;
import com.example.demo.Repository.PaymentsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
@Slf4j
public class PaymentsViewController {

    private final PaymentsRepository paymentsRepository;
    private final GoodsReserveService goodsReserveService;
    @GetMapping("/pay")
    public String paymentPage(@RequestParam Long reserveId, Model model) {
        // 1) 예약 조회
        GoodsReserve reserve = goodsReserveService.findEntityById(reserveId);

        // 2) 금액은 35000 고정
        model.addAttribute("amount", 35000);

        // 3) orderId 생성
        String orderId = "GOODS_" + reserveId + "_" + System.currentTimeMillis();
        model.addAttribute("orderId", orderId);

        // 4) reserveId도 넘기기
        model.addAttribute("reserveId", reserveId);

        return "payments/Payments";

    }

    @GetMapping("/pay/success")
    public String paymentSuccess(@RequestParam String orderId, Model model) {

        Payments payment = paymentsRepository.findByOrderId(orderId)
                .orElseThrow(() -> new IllegalArgumentException("결제 정보를 찾을 수 없습니다."));

        model.addAttribute("orderId", payment.getOrderId());
        model.addAttribute("amount", payment.getAmount());
        model.addAttribute("method", payment.getMethod());

        return "payments/PaymentsSuccess";
    }
    @GetMapping("/pay/fail")
    public String paymentFail(@RequestParam(required = false) String orderId,
                              Model model) {
        model.addAttribute("orderId", orderId);
        return "payments/PaymentsFailure";
    }
}

