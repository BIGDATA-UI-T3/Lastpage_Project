package com.example.demo.Repository;

import com.example.demo.Domain.Common.Entity.Payments;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentsRepository extends JpaRepository<Payments, String> {

    /** 주문번호로 조회 */
    Optional<Payments> findByOrderId(String orderId);

    /** 사용자별 결제 내역 조회 (최신순) */
    List<Payments> findByUser_UserSeqOrderByCreatedAtDesc(String userSeq);

    /** 결제 상태로 조회 (READY, SUCCESS, FAILED 등) */
    List<Payments> findAllByStatus(String status);
}
