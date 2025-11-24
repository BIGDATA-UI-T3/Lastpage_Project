package com.example.demo.Controller;

import com.example.demo.Domain.Common.Entity.Payments;
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
    @GetMapping("/pay")
    public String paymentPage(Model model) {
        // 필요시 결제 금액, 주문번호 등을 model에 담아서 전송 가능
        model.addAttribute("amount", 35000);
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

