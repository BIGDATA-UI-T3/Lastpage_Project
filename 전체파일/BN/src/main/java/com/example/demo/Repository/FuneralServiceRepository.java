package com.example.demo.Repository;

import com.example.demo.Domain.Common.Entity.FuneralService;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FuneralServiceRepository extends JpaRepository<FuneralService, Long> {

    // [추가] 이름(Name) 또는 주소(Address)에 키워드가 포함(Containing)되어 있으면 검색
    List<FuneralService> findByNameContainingOrAddressContaining(String nameKeyword, String addressKeyword);
}