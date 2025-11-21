package com.example.demo.Repository;

import com.example.demo.Domain.Common.Entity.FuneralReserve;
import com.example.demo.Domain.Common.Entity.PsyReserve;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;


public interface FuneralReserveRepository extends JpaRepository <FuneralReserve, Long> {
    Optional<FuneralReserve> findById(Long id);
    Optional<FuneralReserve> findByUser_UserSeq(String userSeq);
    List<FuneralReserve> findAllByUser_UserSeq(String userSeq);
}