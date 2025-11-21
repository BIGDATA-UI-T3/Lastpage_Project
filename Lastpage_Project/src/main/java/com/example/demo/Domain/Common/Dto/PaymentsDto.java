package com.example.demo.Domain.Common.Dto;

import lombok.Data;

/**
 * 통합 결제 요청 DTO
 * (토스 / 카카오 / 네이버 / 카드 결제 공통 사용)
 */
@Data
public class PaymentsDto {

    /** 주문 고유번호 (예약 or 주문 단위 식별자) */
    private String orderId;

    /** 주문명 또는 상품명 */
    private String orderName;

    /** 결제 금액 */
    private Integer amount;

    /** 결제 방식 (CARD / TOSS / KAKAO / NAVER) */
    private String method;

    /** 결제자 회원 식별자 (user_info.user_seq) */
    private String userSeq;

    /** 결제 성공 시 이동할 URL */
    private String successUrl;

    /** 결제 실패 시 이동할 URL */
    private String failUrl;

    /** 브라우저나 클라이언트 식별용 (선택사항) */
    private String clientType;

    /**
     * 결제 요청 시 자동 URL 설정을 위한 유틸
     * (프론트엔드에서 따로 지정하지 않아도 기본 성공/실패 경로 자동 설정)
     */
    public void applyDefaultUrls(String baseUrl) {
        this.successUrl = baseUrl + "/pay/success?orderId=" + orderId;
        this.failUrl = baseUrl + "/pay/fail?orderId=" + orderId;
    }
}
