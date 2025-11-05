package com.example.demo.Repository;

import com.example.demo.Domain.Common.Entity.FuneralReserve;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;


public interface FuneralReserveRepository extends JpaRepository <FuneralReserve, Long> {
    // 예약자 기준으로 예약 내역 조회
    List<FuneralReserve> findByMemberUsername(String userName);
    // 예약 ID와 회원 username으로 단일 예약 조회
    Optional<FuneralReserve> findByIdAndMemberUsername(Long id, String username);

}