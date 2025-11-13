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

import java.net.URI;
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

    //  KakaoPay 정보 주입
    @Value("${kakaopay.admin-key}")
    private String kakaoAdminKey;

    @Value("${kakaopay.cid}")
    private String kakaoCid;

    @Value("${kakaopay.approval-url}")
    private String approvalUrl;

    @Value("${kakaopay.cancel-url}")
    private String cancelUrl;

    @Value("${kakaopay.fail-url}")
    private String failUrl;

    /** -----------------------------------------------------
     * 결제 생성 및 PG사 요청 (토스 / 카카오 / 네이버 / 카드)
     * ----------------------------------------------------- */
    @Transactional
    public Map<String, Object> createPayment(PaymentsDto dto) {
        log.info("결제 요청 수신: method={}, orderId={}, userSeq={}",
                dto.getMethod(), dto.getOrderId(), dto.getUserSeq());

        Signup user = signupRepository.findById(dto.getUserSeq())
                .orElseThrow(() -> new IllegalArgumentException("회원 정보를 찾을 수 없습니다."));

        // [1] 결제 엔티티 저장
        Payments payment = Payments.builder()
                .orderId(dto.getOrderId())
                .orderName(dto.getOrderName() != null ? dto.getOrderName() : "LastPage 주문")
                .amount(dto.getAmount())
                .method(dto.getMethod())
                .status("READY")
                .user(user)
                .createdAt(LocalDateTime.now())
                .build();

        paymentsRepository.save(payment);
        log.info("결제 정보 저장 완료: {}", payment.getOrderId());

        Map<String, Object> response = new HashMap<>();

        switch (dto.getMethod().toUpperCase()) {

            /** -------------------- 카드 결제 -------------------- */
            case "CARD" -> {
                response.put("status", "SUCCESS");
                response.put("message", "카드 결제가 정상 처리되었습니다.");
                payment.setStatus("SUCCESS");
            }

            /** -------------------- 카카오페이 -------------------- */
            case "KAKAO" -> {
                try {
                    //  카카오페이 결제 준비 API 호출
                    String kakaoReadyUrl = "https://kapi.kakao.com/v1/payment/ready";

                    HttpHeaders headers = new HttpHeaders();
                    headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
                    headers.set("Authorization", "KakaoAK " + kakaoAdminKey);

                    MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
                    params.add("cid", kakaoCid);
                    params.add("partner_order_id", dto.getOrderId());
                    params.add("partner_user_id", user.getUserSeq());
                    params.add("item_name",
                            dto.getOrderName() != null && !dto.getOrderName().isEmpty()
                                    ? dto.getOrderName()
                                    : "LastPage 상품결제"
                    );

                    params.add("quantity", "1");
                    params.add("total_amount", String.valueOf(dto.getAmount()));
                    params.add("vat_amount", "0");
                    params.add("tax_free_amount", "0");
                    params.add("approval_url", approvalUrl + "?orderId=" + dto.getOrderId());
                    params.add("cancel_url", cancelUrl + "?orderId=" + dto.getOrderId());
                    params.add("fail_url", failUrl + "?orderId=" + dto.getOrderId());

                    HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

                    ResponseEntity<Map> kakaoResponse = restTemplate.exchange(
                            kakaoReadyUrl,
                            HttpMethod.POST,
                            request,
                            Map.class
                    );

                    if (kakaoResponse.getStatusCode() == HttpStatus.OK) {
                        Map<String, Object> body = kakaoResponse.getBody();
                        assert body != null;

                        response.put("next_redirect_pc_url", body.get("next_redirect_pc_url"));
                        response.put("tid", body.get("tid"));
                        payment.setPgTransactionId((String) body.get("tid"));
                        log.info("카카오페이 결제 준비 완료: {}", body.get("tid"));
                    } else {
                        throw new RuntimeException("카카오페이 결제 준비 실패: " + kakaoResponse.getStatusCode());
                    }

                } catch (Exception e) {
                    log.error("카카오페이 결제 요청 중 오류", e);
                    throw new RuntimeException("카카오페이 결제 요청 실패: " + e.getMessage());
                }
            }

            case "NAVER" -> {
                String clientId = "HN3GGCMDdTgGUfl0kFCo";
                String clientSecret = "ftZjkkRNMR";
                String chainId = "aEhSaHZZazhwc29";
                String partnerId = "np_cmyhk063738";
                String apiDomain = "dev.apis.naver.com"; // 운영 시 apis.naver.com
                String apiUrl = String.format("https://%s/%s/naverpay/payments/v2/prepare", apiDomain, partnerId);
                String baseUrl = "http://localhost:8090";

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                headers.set("X-Naver-Client-Id", clientId);
                headers.set("X-Naver-Client-Secret", clientSecret);
                headers.set("X-NaverPay-Chain-Id", chainId);

                //  네이버페이 요청 Body
                Map<String, Object> body = new HashMap<>();
                body.put("merchantPayKey", dto.getOrderId());               // 주문 고유 키
                body.put("merchantUserKey", dto.getUserSeq());              // 회원 고유키
                body.put("productName", dto.getOrderName());                // 상품명
                body.put("totalPayAmount", dto.getAmount());                // 총 결제금액
                body.put("taxScopeAmount", dto.getAmount());                // 과세금액
                body.put("taxExScopeAmount", 0);                            // 비과세금액
                body.put("returnUrl", baseUrl + "/api/pay/success?orderId=" + dto.getOrderId());
                body.put("cancelUrl", baseUrl + "/api/pay/cancel?orderId=" + dto.getOrderId());
                body.put("failUrl", baseUrl + "/api/pay/fail?orderId=" + dto.getOrderId());

                HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
                RestTemplate restTemplate = new RestTemplate();

                try {
                    ResponseEntity<Map> resp = restTemplate.postForEntity(apiUrl, request, Map.class);
                    Map<String, Object> respBody = resp.getBody();
                    log.info("네이버페이 결제 준비 응답: {}", respBody);

                    //  네이버 응답 구조 예시:
                    // {
                    //   "code": "Success",
                    //   "message": "요청 성공",
                    //   "body": {
                    //     "paymentUrl": "https://order.pay.naver.com/pay/...redirectUrl...",
                    //     "reserveId": "예약ID"
                    //   }
                    // }

                    if (respBody != null && "Success".equals(respBody.get("code"))) {
                        Map<String, Object> bodyMap = (Map<String, Object>) respBody.get("body");
                        String paymentUrl = (String) bodyMap.get("paymentUrl");
                        String reserveId = (String) bodyMap.get("reserveId");

                        response.put("redirect_url", paymentUrl);
                        response.put("status", "READY");
                        payment.setPgTransactionId(reserveId);
                        payment.setStatus("READY");
                    } else {
                        throw new IllegalArgumentException("네이버페이 준비요청 실패: " + respBody);
                    }

                } catch (HttpClientErrorException e) {
                    log.error("네이버페이 API 오류: {}", e.getResponseBodyAsString());
                    throw new IllegalArgumentException("네이버페이 요청 실패: " + e.getResponseBodyAsString());
                }
            }

            /** -------------------- 토스페이 -------------------- */
            case "TOSS" -> {
                String tossUrl = "https://pay.toss.im/mock/redirect/" + dto.getOrderId();
                response.put("toss_redirect_url", tossUrl);
                response.put("status", "READY");
                payment.setPgTransactionId("TOSS_" + dto.getOrderId());
            }

            default -> throw new IllegalArgumentException("지원하지 않는 결제 방식입니다: " + dto.getMethod());
        }

        paymentsRepository.save(payment);
        return response;
    }

    /** -----------------------------------------------------
     * 결제 성공 처리 (PG사 콜백용)
     * ----------------------------------------------------- */
    @Transactional
    public void approvePayment(String orderId) {
        Payments payment = paymentsRepository.findByOrderId(orderId)
                .orElseThrow(() -> new IllegalArgumentException("결제 정보를 찾을 수 없습니다."));
        payment.setStatus("SUCCESS");
        payment.setApprovedAt(LocalDateTime.now());
        paymentsRepository.save(payment);
        log.info("[결제 승인 완료] orderId={}", orderId);
    }

    /** -----------------------------------------------------
     * 결제 실패 처리
     * ----------------------------------------------------- */
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
