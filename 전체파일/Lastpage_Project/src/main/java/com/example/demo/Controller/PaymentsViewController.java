package com.example.demo.Controller;

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
    @GetMapping("/pay")
    public String paymentPage(Model model) {
        // 필요시 결제 금액, 주문번호 등을 model에 담아서 전송 가능
        model.addAttribute("amount", 10);
        model.addAttribute("orderId", "ORDER_1234");
        return "payments/Payments";
    }

    @GetMapping("/pay/success")
    public String paymentSuccess(@RequestParam(required = false) String orderId,
                                 @RequestParam(required = false) Integer amount,
                                 @RequestParam(required = false) String method,
                                 Model model) {
        model.addAttribute("orderId", orderId);
        model.addAttribute("amount", amount);
        model.addAttribute("method", method);
        return "payments/PaymentsSuccess";
    }

    @GetMapping("/pay/fail")
    public String paymentFail(@RequestParam(required = false) String orderId,
                              Model model) {
        model.addAttribute("orderId", orderId);
        return "payments/PaymentsFailure";
    }
}

