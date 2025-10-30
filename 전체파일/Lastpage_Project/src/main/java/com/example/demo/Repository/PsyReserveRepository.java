package com.example.demo.Repository;

import com.example.demo.Domain.Common.Entity.PsyReserve;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PsyReserveRepository extends JpaRepository <PsyReserve, Long> {
    Optional<PsyReserve> findByEmail(String email);
    // 날짜 + 시간으로 이미 있는지
    boolean existsByConsultDateAndTime(String consultDate, String time);
    // 날짜 + 시간으로 여러 개 (수정 중 자기 것 제외용)
    List<PsyReserve> findByConsultDateAndTime(String consultDate, String time);
    // 날짜로 전부 가져오기 (자바스크립트에 넘겨줄 때 사용)
    List<PsyReserve> findByConsultDate(String consultDate);



}