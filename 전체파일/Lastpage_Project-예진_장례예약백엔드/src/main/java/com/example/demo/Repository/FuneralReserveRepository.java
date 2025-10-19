package com.example.demo.Repository;

import com.example.demo.Domain.Common.Entity.FuneralReserve;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


public interface FuneralReserveRepository extends JpaRepository <FuneralReserve, Long> {
    // 예약자 기준으로 예약 내역 조회 (예시)
    List<FuneralReserve> findByOwnerName(String ownerName);
}