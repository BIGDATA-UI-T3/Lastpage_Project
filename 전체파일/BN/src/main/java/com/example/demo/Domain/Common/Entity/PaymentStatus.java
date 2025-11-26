package com.example.demo.Domain.Common.Entity;

public enum PaymentStatus {
    UNPAID,   // 결제 전
    PAID,     // 결제 완료
    CANCELLED // 결제 취소(추후 환불 기능 대비)
}

