package com.example.demo.Repository;

import com.example.demo.Domain.Common.Entity.GoodsReserve;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GoodsReserveRepository extends JpaRepository <GoodsReserve, Long> {

    List<GoodsReserve> findByUserUsername(String username);
}