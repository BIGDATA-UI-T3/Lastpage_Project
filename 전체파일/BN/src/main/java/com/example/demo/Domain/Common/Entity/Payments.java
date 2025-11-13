package com.example.demo.Domain.Common.Entity;
import com.example.demo.Domain.Common.Entity.Signup;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Payments {

    /** 결제 고유 식별자 */
    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "payment_id", length = 36, nullable = false, updatable = false, unique = true)
    private String paymentId;

    /** 주문번호 (PG사 통신용) */
    @Column(name = "order_id", nullable = false)
    private String orderId;

    /** 결제금액 */
    @Column(nullable = false)
    private Integer amount;

    /** 결제수단 (TOSS, KAKAO, NAVER 등) */
    @Column(nullable = false, length = 20)
    private String method;

    /** 결제상태 (READY, SUCCESS, FAIL, CANCEL 등) */
    @Column(nullable = false, length = 20)
    private String status;

    /** 결제 승인번호 (Toss paymentKey, Kakao tid 등) */
    @Column(name = "pg_transaction_id", length = 100)
    private String pgTransactionId;

    /** 주문명 */
    @Column(name = "order_name", length = 200)
    private String orderName;

    /** 생성일시 */
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    /** 승인일시 */
    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    /** 수정일시 */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    /** 결제취소여부 */
    @Column(nullable = false)
    private boolean canceled;

    /** 취소시각 */
    private LocalDateTime canceledAt;

    /* -----------------------------------------------
     *  회원정보 (Signup)와의 연관관계 설정
     * ----------------------------------------------- */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_seq", nullable = false) //
    private Signup user;
}
