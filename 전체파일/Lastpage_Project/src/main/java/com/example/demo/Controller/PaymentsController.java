package com.example.demo.Controller;

import com.example.demo.Domain.Common.Dto.PaymentsDto;
import com.example.demo.Domain.Common.Service.PaymentsService;
import com.example.demo.Repository.PaymentsRepository;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Controller
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Slf4j
public class PaymentsController {

    private final PaymentsService paymentsService;
    private final PaymentsRepository paymentsRepository;

    /* ==========================================================
     * 1) 결제 요청 공통 엔트리포인트 (카카오 / 네이버)
     * ========================================================== */
    @PostMapping("/create")
    @ResponseBody
    public ResponseEntity<?> createPayment(@RequestBody PaymentsDto dto,
                                           HttpSession session) {
        try {
            Object loginUser = session.getAttribute("loginUser");
            if (loginUser == null) {
                return ResponseEntity.status(401).body("로그인이 필요합니다.");
            }

            // 회원번호 추출
            String userSeq = null;
            if (loginUser instanceof com.example.demo.Domain.Common.Entity.Signup user) {
                userSeq = user.getUserSeq();
            } else if (loginUser instanceof com.example.demo.Domain.Common.Dto.SignupDto userDto) {
                userSeq = userDto.getUserSeq();
            } else {
                return ResponseEntity.badRequest().body("유효하지 않은 로그인 세션입니다.");
            }

            dto.setUserSeq(userSeq);

            // 결제 생성 및 PG사 Ready 호출
            Map<String, Object> response = paymentsService.createPayment(dto);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("[ERROR] 결제 요청 중 내부 오류", e);
            return ResponseEntity.internalServerError()
                    .body("결제 처리 중 오류가 발생했습니다.");
        }
    }



    /* ==========================================================
     *  2) 카카오페이 콜백
     * ========================================================== */

    /** 카카오페이 결제 승인 성공 */
    @GetMapping("/kakao/success")
    public String kakaoPaymentSuccess(@RequestParam String orderId) {

        log.info("[카카오 결제 성공 콜백] orderId={}", orderId);

        paymentsService.approvePayment(orderId);

        return "redirect:/pay/success?orderId=" + orderId;
    }

    /** 카카오페이 취소 */
    @GetMapping("/kakao/cancel")
    public String kakaoPaymentCancel(@RequestParam String orderId) {

        log.info("[카카오 결제 취소 콜백] orderId={}", orderId);

        paymentsService.failPayment(orderId, "사용자 결제 취소(카카오)");

        return "redirect:/pay/fail?orderId=" + orderId;
    }

    /** 카카오페이 실패 */
    @GetMapping("/kakao/fail")
    public String kakaoPaymentFail(@RequestParam String orderId,
                                   @RequestParam(required = false) String reason) {

        log.info("[카카오 결제 실패 콜백] orderId={}, reason={}", orderId, reason);

        paymentsService.failPayment(orderId,
                reason != null ? reason : "카카오 결제 실패");

        return "redirect:/pay/fail?orderId=" + orderId;
    }

}
