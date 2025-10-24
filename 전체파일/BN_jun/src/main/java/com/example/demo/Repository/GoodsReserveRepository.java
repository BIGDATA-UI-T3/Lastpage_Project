package com.example.demo.Repository;

import com.example.demo.Domain.Common.Entity.GoodsReserve;
import org.springframework.data.jpa.repository.JpaRepository;

// @Repository 어노테이션을 붙여주는 것이 좋습니다.
import org.springframework.stereotype.Repository;

@Repository
public interface GoodsReserveRepository extends JpaRepository <GoodsReserve, Long> {
}