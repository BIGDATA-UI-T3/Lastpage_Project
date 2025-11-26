package com.example.demo.Domain.Common.Service;

import com.example.demo.Domain.Common.Dto.PaymentsDto;
import com.example.demo.Domain.Common.Entity.Payments;
import com.example.demo.Domain.Common.Entity.Signup;
import com.example.demo.Repository.PaymentsRepository;
import com.example.demo.Repository.SignupRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentsService {

    private final PaymentsRepository paymentsRepository;
    private final SignupRepository signupRepository;
    private final RestTemplate restTemplate = new RestTemplate();
    private final GoodsReserveService goodsReserveService;

    /* ---------------------------------------
     * KakaoPay 설정
     * --------------------------------------- */
    @Value("${kakaopay.admin-key}")
    private String kakaoAdminKey;

    @Value("${kakaopay.cid}")
    private String kakaoCid;

    @Value("${kakaopay.approval-url}")
    private String kakaoApprovalUrl;

    @Value("${kakaopay.cancel-url}")
    private String kakaoCancelUrl;

    @Value("${kakaopay.fail-url}")
    private String kakaoFailUrl;


    /* ---------------------------------------
     * NaverPay 설정
     * --------------------------------------- */
    @Value("${naver.pay.client-id}")
    private String naverClientId;

    @Value("${naver.pay.client-secret}")
    private String naverClientSecret;

    @Value("${naver.pay.chain-id}")
    private String naverChainId;

    @Value("${naver.pay.partner-id}")
    private String naverPartnerId;

    @Value("${naver.pay.api-domain}")
    private String naverApiDomain;

    @Value("${server.base-url}")
    private String baseUrl;



    /* ==========================================================
     * 1) 결제 생성 (카카오 / 네이버)
     * ========================================================== */
    @Transactional
    public Map<String, Object> createPayment(PaymentsDto dto) {

        log.info("[결제 생성 요청] method={}, orderId={}, userSeq={}",
                dto.getMethod(), dto.getOrderId(), dto.getUserSeq());

        Signup user = signupRepository.findById(dto.getUserSeq())
                .orElseThrow(() -> new IllegalArgumentException("회원 정보를 찾을 수 없습니다."));

        String method = dto.getMethod().toUpperCase();

        String orderName =
                (dto.getOrderName() == null || dto.getOrderName().isBlank())
                        ? "LastPage 상품결제"
                        : dto.getOrderName();

        /* ========== 결제 엔티티 저장 ========== */
        Payments payment = Payments.builder()
                .orderId(dto.getOrderId())
                .reserveId(dto.getReserveId())   // ★ 추가
                .orderName(orderName)
                .amount(dto.getAmount())
                .method(method)
                .status("READY")
                .user(user)
                .createdAt(LocalDateTime.now())
                .build();

        paymentsRepository.save(payment);

        Map<String, Object> response = new HashMap<>();


        switch (method) {

            /* =====================================================
             * KAKAO PAY READY
             * ===================================================== */
            case "KAKAO" -> {

                try {
                    String url = "https://kapi.kakao.com/v1/payment/ready";

                    HttpHeaders headers = new HttpHeaders();
                    headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
                    headers.set("Authorization", "KakaoAK " + kakaoAdminKey);

                    MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
                    params.add("cid", kakaoCid);
                    params.add("partner_order_id", dto.getOrderId());
                    params.add("partner_user_id", user.getUserSeq());
                    params.add("item_name", orderName);
                    params.add("quantity", "1");
                    params.add("total_amount", String.valueOf(dto.getAmount()));
                    params.add("tax_free_amount", "0");

                    params.add("approval_url", kakaoApprovalUrl + "?orderId=" + dto.getOrderId());
                    params.add("cancel_url", kakaoCancelUrl + "?orderId=" + dto.getOrderId());
                    params.add("fail_url", kakaoFailUrl + "?orderId=" + dto.getOrderId());

                    HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

                    ResponseEntity<Map> kakaoResponse =
                            restTemplate.exchange(url, HttpMethod.POST, request, Map.class);

                    if (kakaoResponse.getStatusCode() == HttpStatus.OK) {

                        Map<String, Object> body = kakaoResponse.getBody();
                        String tid = (String) body.get("tid");

                        payment.setPgTransactionId(tid);
                        response.put("next_redirect_pc_url", body.get("next_redirect_pc_url"));

                    } else {
                        throw new RuntimeException("카카오페이 결제 준비 실패");
                    }

                } catch (Exception e) {
                    log.error("[카카오페이 오류]", e);
                    throw new RuntimeException("카카오페이 오류: " + e.getMessage());
                }
            }


            /* =====================================================
             * NAVER PAY READY
             * ===================================================== */
            case "NAVER" -> {

                try {
                    String url = String.format(
                            "https://%s/%s/naverpay/payments/v2/prepare",
                            naverApiDomain, naverPartnerId
                    );

                    HttpHeaders headers = new HttpHeaders();
                    headers.setContentType(MediaType.APPLICATION_JSON);
                    headers.set("X-Naver-Client-Id", naverClientId);
                    headers.set("X-Naver-Client-Secret", naverClientSecret);
                    headers.set("X-NaverPay-Chain-Id", naverChainId);

                    Map<String, Object> body = new HashMap<>();
                    body.put("merchantPayKey", dto.getOrderId());
                    body.put("merchantUserKey", dto.getUserSeq());
                    body.put("productName", orderName);
                    body.put("totalPayAmount", dto.getAmount());
                    body.put("taxScopeAmount", dto.getAmount());
                    body.put("taxExScopeAmount", 0);

                    body.put("returnUrl", baseUrl + "/api/payments/naver/success?orderId=" + dto.getOrderId());
                    body.put("cancelUrl", baseUrl + "/api/payments/naver/cancel?orderId=" + dto.getOrderId());
                    body.put("failUrl", baseUrl + "/api/payments/naver/fail?orderId=" + dto.getOrderId());

                    HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

                    ResponseEntity<Map> resp =
                            restTemplate.postForEntity(url, request, Map.class);

                    Map<String, Object> respBody = resp.getBody();

                    if (respBody != null && "Success".equals(respBody.get("code"))) {

                        Map<String, Object> bodyMap = (Map<String, Object>) respBody.get("body");

                        payment.setPgTransactionId((String) bodyMap.get("reserveId"));
                        response.put("redirect_url", bodyMap.get("paymentUrl"));

                    } else {
                        throw new RuntimeException("네이버페이 준비 실패: " + respBody);
                    }

                } catch (HttpClientErrorException e) {
                    log.error("[네이버페이 오류] {}", e.getResponseBodyAsString());
                    throw new RuntimeException("네이버페이 오류: " + e.getResponseBodyAsString());
                }
            }

            default -> {
                throw new IllegalArgumentException("지원하지 않는 결제방식: " + method);
            }
        }

        paymentsRepository.save(payment);
        return response;
    }



    /* ==========================================================
     * 2) 결제 승인 처리
     * ========================================================== */
    @Transactional
    public void approvePayment(String orderId) {

        Payments payment = paymentsRepository.findByOrderId(orderId)
                .orElseThrow(() -> new IllegalArgumentException("결제 정보를 찾을 수 없습니다."));

        payment.setStatus("SUCCESS");
        payment.setApprovedAt(LocalDateTime.now());

        paymentsRepository.save(payment);

        log.info("[결제 승인 완료] orderId={}", orderId);
        log.info("[결제 승인 처리] orderId={}, reserveId={}", orderId, payment.getReserveId());




        //  여기서 goods_reserve 상태 변경
        Long reserveId = payment.getReserveId();
        if (reserveId != null) {
            goodsReserveService.updateStatus(reserveId, "PAID");
        } else {
            log.warn("[주의] reserveId 없음 — goods_reserve 업데이트 불가");
        }
    }



    /* ==========================================================
     * 3) 결제 실패 처리
     * ========================================================== */
    @Transactional
    public void failPayment(String orderId, String reason) {

        Payments payment = paymentsRepository.findByOrderId(orderId)
                .orElseThrow(() -> new IllegalArgumentException("결제 정보를 찾을 수 없습니다."));

        payment.setStatus("FAILED");
        payment.setApprovedAt(LocalDateTime.now());

        paymentsRepository.save(payment);

        log.warn("[결제 실패 처리] orderId={}, reason={}", orderId, reason);
    }
}
