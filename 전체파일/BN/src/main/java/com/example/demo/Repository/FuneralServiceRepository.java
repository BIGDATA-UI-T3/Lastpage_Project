package com.example.demo.Repository;

import com.example.demo.Domain.Common.Entity.FuneralService;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * FuneralPlace Entity에 대한 데이터베이스 접근(CRUD)을 처리합니다.
 */
@Repository
public interface FuneralServiceRepository extends JpaRepository<FuneralService, Long> {
    // JpaRepository가 findById, findAll, saveAll 등을 기본 제공합니다.
}