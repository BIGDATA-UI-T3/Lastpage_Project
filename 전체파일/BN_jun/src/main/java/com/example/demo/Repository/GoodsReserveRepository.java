package com.example.demo.Repository;

import com.example.demo.Domain.Common.Entity.GoodsReserve;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GoodsReserveRepository extends JpaRepository <GoodsReserve, Long> {

    // username으로 예약 목록 찾기
    List<GoodsReserve> findByUserUsername(String username);

    @Query("SELECT gr FROM GoodsReserve gr JOIN FETCH gr.user WHERE gr.id = :id")
    Optional<GoodsReserve> findByIdWithUser(@Param("id") Long id);
}