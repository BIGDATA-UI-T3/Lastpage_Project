package com.example.demo.Repository;

import com.example.demo.Domain.Common.Entity.Payments;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentsRepository extends JpaRepository<Payments, String> {

    /** 주문번호로 조회 */
    Optional<Payments> findByOrderId(String orderId);

    /** 사용자별 결제내역 조회 */


    /** 특정 상태의 결제 조회 (예: READY, SUCCESS 등) */
    List<Payments> findAllByStatus(String status);
    @Query("SELECT s FROM Signup s ORDER BY s.created_at DESC")
    List<Payments> findAllByUser_UserSeqOrderByCreated_atDesc(String userSeq);

}
