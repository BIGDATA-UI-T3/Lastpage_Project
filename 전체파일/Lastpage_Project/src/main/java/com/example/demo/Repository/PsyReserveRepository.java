package com.example.demo.Repository;

import com.example.demo.Domain.Common.Entity.PsyReserve;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PsyReserveRepository extends JpaRepository<PsyReserve, Long> {
    Optional<PsyReserve> findById(Long id);
    Optional<PsyReserve> findByUser_UserSeq(String userSeq);
    boolean existsByConsultDateAndTime(String consultDate, String time);
    List<PsyReserve> findByConsultDateAndTime(String consultDate, String time);
    List<PsyReserve> findByConsultDate(String consultDate);
    List<PsyReserve> findAllByUser_UserSeq(String userSeq);

}
