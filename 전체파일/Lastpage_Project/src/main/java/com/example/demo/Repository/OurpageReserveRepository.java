package com.example.demo.Repository;

import com.example.demo.Domain.Common.Entity.OurpageReserve;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OurpageReserveRepository extends JpaRepository<OurpageReserve, Long> {
    // 필요한 경우 추가 메서드 작성
}