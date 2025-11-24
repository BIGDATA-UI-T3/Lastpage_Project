package com.example.demo.Controller;

import com.example.demo.Domain.Common.Dto.PaymentsDto;
import com.example.demo.Domain.Common.Service.PaymentsService;
import com.example.demo.Repository.PaymentsRepository;
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
    private final PaymentsRepository paymentsRepository;

    /** ----------------------------------------------------------
     *  1) 결제 요청 (카카오 / 네이버 공통 진입점)
     *  - dto.pgType 등으로 "KAKAO", "NAVER" 구분해서
     *    PaymentsService.createPayment() 내부에서 분기하도록 사용
     * ---------------------------------------------------------- */
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

            // ★ 여기서 기본 콜백 URL을 PG별로 분기해서 넣도록 구현하면 좋아요
            //   (예: dto.applyDefaultUrls("http://localhost:8090", "KAKAO") 이런 식으로)
            dto.applyDefaultUrls("http://localhost:8090");

            Map<String, Object> response = paymentsService.createPayment(dto);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("결제 요청 중 오류 발생", e);
            return ResponseEntity.internalServerError().body("결제 처리 중 오류가 발생했습니다.");
        }
    }

    /* ==========================================================
     *  2) 카카오페이 콜백
     *     - approval_url  : /api/payments/kakao/success?orderId=...
     *     - cancel_url    : /api/payments/kakao/cancel?orderId=...
     *     - fail_url      : /api/payments/kakao/fail?orderId=...
     * ========================================================== */

    /** 카카오페이 결제 성공 콜백 (GET) */
    @GetMapping("/kakao/success")
    public String kakaoPaymentSuccess(@RequestParam String orderId) {

        log.info("[카카오 결제 성공 콜백] orderId={}", orderId);

        // orderId 기준으로 kakao /approve 호출 및 DB 상태 갱신
        paymentsService.approvePayment(orderId);

        // 실제 결제 완료 화면으로 리다이렉트
        return "redirect:/pay/success?orderId=" + orderId;
    }

    /** 카카오페이 결제 취소 콜백 (GET) */
    @GetMapping("/kakao/cancel")
    public String kakaoPaymentCancel(@RequestParam String orderId) {

        log.info("[카카오 결제 취소 콜백] orderId={}", orderId);

        paymentsService.failPayment(orderId, "사용자 결제 취소(카카오)");

        return "redirect:/pay/fail?orderId=" + orderId;
    }

    /** 카카오페이 결제 실패 콜백 (GET) */
    @GetMapping("/kakao/fail")
    public String kakaoPaymentFail(@RequestParam String orderId,
                                   @RequestParam(required = false) String reason) {

        log.info("[카카오 결제 실패 콜백] orderId={}, reason={}", orderId, reason);

        paymentsService.failPayment(orderId,
                reason != null ? reason : "카카오 결제 실패");

        return "redirect:/pay/fail?orderId=" + orderId;
    }




}
