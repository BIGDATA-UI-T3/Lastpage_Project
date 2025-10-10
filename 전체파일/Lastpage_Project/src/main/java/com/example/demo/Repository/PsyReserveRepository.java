package com.example.demo.Repository;

import com.example.demo.Domain.Common.Entity.PsyReserve;
import org.springframework.data.jpa.repository.JpaRepository;


public interface PsyReserveRepository extends JpaRepository <PsyReserve, Long> {
}