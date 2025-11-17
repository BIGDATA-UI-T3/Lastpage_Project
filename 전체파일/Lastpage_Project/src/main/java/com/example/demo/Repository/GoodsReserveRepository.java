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

    // userSeq으로 예약 목록 찾기
    List<GoodsReserve> findAllByUser_UserSeq(String userSeq);
    Optional<GoodsReserve> findByUser_UserSeq(String userSeq);
    Optional<GoodsReserve> findById(Long id);

    @Query("SELECT gr FROM GoodsReserve gr JOIN FETCH gr.user WHERE gr.id = :id")
    Optional<GoodsReserve> findByIdWithUser(@Param("id") Long id);

    @Query("SELECT s FROM Signup s ORDER BY s.created_at DESC")
    List<GoodsReserve> findTop5ByOrderByCreated_atDesc();

    long count();
}