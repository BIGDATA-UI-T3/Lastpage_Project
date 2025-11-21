package com.example.demo.Controller;

import com.example.demo.Domain.Common.Dto.PaymentsDto;
import com.example.demo.Domain.Common.Service.PaymentsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import java.util.Map;

@Controller
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Slf4j
public class PaymentsController {

    private final PaymentsService paymentsService;

    /** -----------------------------------------
     *  결제 요청 (카드 / 토스 / 카카오 / 네이버)
     * ----------------------------------------- */
    @PostMapping("/create")
    @ResponseBody
    public ResponseEntity<?> createPayment(@RequestBody PaymentsDto dto,
                                           HttpSession session) {
        try {
            Object loginUser = session.getAttribute("loginUser");
            if (loginUser == null) {
                return ResponseEntity.status(401).body("로그인이 필요합니다.");
            }

            String userSeq = null;
            if (loginUser instanceof com.example.demo.Domain.Common.Entity.Signup user) {
                userSeq = user.getUserSeq();
            } else if (loginUser instanceof com.example.demo.Domain.Common.Dto.SignupDto userDto) {
                userSeq = userDto.getUserSeq();
            } else {
                return ResponseEntity.badRequest().body("유효하지 않은 세션입니다.");
            }

            dto.setUserSeq(userSeq);
            dto.applyDefaultUrls("http://localhost:8090"); // 기본 리다이렉트 URL 자동 구성

            Map<String, Object> response = paymentsService.createPayment(dto);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("결제 요청 중 오류 발생", e);
            return ResponseEntity.internalServerError().body("결제 처리 중 오류가 발생했습니다.");
        }
    }

    /** -----------------------------------------
     *  결제 성공 콜백 (PG사 → 서버)
     * ----------------------------------------- */
    @GetMapping("/success")
    public String paymentSuccess(@RequestParam String orderId) {
        paymentsService.approvePayment(orderId);
        return "redirect:/pay/success?orderId=" + orderId; // 결제 성공 페이지로 이동
    }

    /** -----------------------------------------
     *  결제 실패 콜백
     * ----------------------------------------- */
    @GetMapping("/fail")
    public String paymentFail(@RequestParam String orderId,
                              @RequestParam(required = false) String reason) {
        paymentsService.failPayment(orderId, reason != null ? reason : "결제 실패");
        return "redirect:/pay/fail?orderId=" + orderId; // 실패 페이지로 이동
    }
}
