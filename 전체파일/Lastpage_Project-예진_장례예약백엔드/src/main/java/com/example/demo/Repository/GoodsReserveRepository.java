package com.example.demo.Repository;

import com.example.demo.Domain.Common.Entity.GoodsReserve;
import org.springframework.data.jpa.repository.JpaRepository;


public interface GoodsReserveRepository extends JpaRepository <GoodsReserve, Long> {
}