package com.example.demo.Domain.Common.Service.Admin;

import com.example.demo.Domain.Common.Dto.PsyReserveDto;
import com.example.demo.Domain.Common.Entity.PsyReserve;
import com.example.demo.Repository.PsyReserveRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminPsyReserveService {

    private final PsyReserveRepository psyReserveRepository;

    /**
     * 전체 심리상담 예약 조회
     */
    public List<PsyReserveDto> getAllPsyReserves() {
        return psyReserveRepository.findAll()
                .stream()
                .map(PsyReserveDto::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * 단건 조회
     */
    public PsyReserveDto getPsyReserveById(Long id) {
        PsyReserve p = psyReserveRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("심리상담 예약을 찾을 수 없습니다. id=" + id));

        return PsyReserveDto.fromEntity(p);
    }

    /**
     * 특정 유저 예약 조회
     */
    public List<PsyReserveDto> getPsyReserveByUserSeq(String userSeq) {

        return psyReserveRepository.findByUser_UserSeq(userSeq)
                .stream()
                .map(PsyReserveDto::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * 최신 예약 N개 조회
     *  - createdAt 기준으로 정렬
     */
    public List<PsyReserveDto> getRecentPsyReserves(int limit) {
        return psyReserveRepository.findAll()
                .stream()
                .sorted(Comparator.comparing(PsyReserve::getId).reversed())
                .limit(limit)
                .map(PsyReserveDto::fromEntity)
                .collect(Collectors.toList());
    }


    /**
     * 전체 예약 개수
     */
    public long countPsyReserves() {
        return psyReserveRepository.count();
    }

    /** ★ 삭제 기능 추가 */
    public void deleteById(Long id) {
        if (!psyReserveRepository.existsById(id)) {
            throw new IllegalArgumentException("삭제할 장례 예약이 존재하지 않습니다. id=" + id);
        }
        psyReserveRepository.deleteById(id);
        log.info("[관리자] 장례 예약 삭제 완료: id={}", id);
    }

    public List<PsyReserveDto> searchPsy(String keyword) {
        return psyReserveRepository.findAll()
                .stream()
                .filter(p ->
                        p.getName().contains(keyword) ||
                                p.getConsultDate().contains(keyword) ||
                                p.getUser().getName().contains(keyword)
                )
                .map(PsyReserveDto::fromEntity)
                .collect(Collectors.toList());
    }

}
