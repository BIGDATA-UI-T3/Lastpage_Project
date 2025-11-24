package com.example.demo.Domain.Common.Service.Admin;

import com.example.demo.Domain.Common.Dto.FuneralReserveDto;
import com.example.demo.Domain.Common.Entity.FuneralReserve;
import com.example.demo.Repository.FuneralReserveRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminFuneralReserveService {

    private final FuneralReserveRepository funeralReserveRepository;

    /**
     * 전체 장례 예약 조회
     */
    public List<FuneralReserveDto> getAllFuneralReserves() {
        return funeralReserveRepository.findAll()
                .stream()
                .map(FuneralReserveDto::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * 단건 조회
     */
    public FuneralReserveDto getFuneralReserveById(Long id) {
        FuneralReserve f = funeralReserveRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("장례 예약을 찾을 수 없습니다. id=" + id));

        return FuneralReserveDto.fromEntity(f);
    }

    /**
     * 특정 유저 예약 조회
     */
    public List<FuneralReserveDto> getFuneralReserveByUserSeq(String userSeq) {
        return funeralReserveRepository.findAll()
                .stream()
                .filter(f -> f.getUser().getUserSeq().equals(userSeq))
                .map(FuneralReserveDto::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * 최신 예약 N개 조회
     */
    public List<FuneralReserveDto> getRecentFuneralReserves(int limit) {
        return funeralReserveRepository.findAll()
                .stream()
                .sorted(Comparator.comparing(FuneralReserve::getId).reversed())
                .limit(limit)
                .map(FuneralReserveDto::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * 전체 예약 개수
     */
    public long countFuneralReserves() {
        return funeralReserveRepository.count();
    }

    /** ★ 삭제 기능 추가 */
    public void deleteById(Long id) {
        if (!funeralReserveRepository.existsById(id)) {
            throw new IllegalArgumentException("삭제할 장례 예약이 존재하지 않습니다. id=" + id);
        }
        funeralReserveRepository.deleteById(id);
        log.info("[관리자] 장례 예약 삭제 완료: id={}", id);
    }
    public List<FuneralReserveDto> searchFuneral(String keyword) {
        return funeralReserveRepository.findAll()
                .stream()
                .filter(f ->
                        f.getOwnerName().contains(keyword) ||
                                f.getPetName().contains(keyword) ||
                                f.getUser().getName().contains(keyword)
                )
                .map(FuneralReserveDto::fromEntity)
                .collect(Collectors.toList());
    }




}
