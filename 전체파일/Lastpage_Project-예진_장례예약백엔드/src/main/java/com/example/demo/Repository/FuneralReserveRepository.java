package com.example.demo.Repository;

import com.example.demo.Domain.Common.Entity.FuneralReserve;
import org.springframework.data.jpa.repository.JpaRepository;


public interface FuneralReserveRepository extends JpaRepository <FuneralReserve, Long> {
}