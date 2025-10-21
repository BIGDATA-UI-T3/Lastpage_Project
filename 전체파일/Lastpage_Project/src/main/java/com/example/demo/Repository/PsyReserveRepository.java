package com.example.demo.Repository;

import com.example.demo.Domain.Common.Entity.PsyReserve;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PsyReserveRepository extends JpaRepository <PsyReserve, Long> {
    Optional<PsyReserve> findByEmail(String email);
}