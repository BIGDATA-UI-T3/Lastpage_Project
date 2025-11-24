package com.example.demo.Domain.Common.Service.Admin;

import com.example.demo.Domain.Common.Dto.GoodsReserveDto;
import com.example.demo.Domain.Common.Entity.GoodsReserve;
import com.example.demo.Repository.GoodsReserveRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminGoodsReserveService {

    private final GoodsReserveRepository goodsReserveRepository;

    /**
     * 전체 굿즈 예약 목록 조회
     */
    public List<GoodsReserveDto> getAllGoodsReserves() {
        return goodsReserveRepository.findAll()
                .stream()
                .map(GoodsReserveDto::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * 특정 예약 단건 조회
     */
    public GoodsReserveDto getGoodsReserveById(Long id) {
        GoodsReserve g = goodsReserveRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 예약을 찾을 수 없습니다. id=" + id));

        return GoodsReserveDto.fromEntity(g);
    }

    /**
     * 특정 유저의 예약 목록 조회
     */
    public List<GoodsReserveDto> getGoodsReserveByUserSeq(String userSeq) {
        return goodsReserveRepository.findAll()
                .stream()
                .filter(g -> g.getUser().getUserSeq().equals(userSeq))
                .map(GoodsReserveDto::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * 최신 예약 N개 조회 (대시보드용)
     */
    public List<GoodsReserveDto> getRecentGoodsReserves(int limit) {
        return goodsReserveRepository.findAll()
                .stream()
                .sorted(Comparator.comparing(GoodsReserve::getId).reversed())  // 최신순
                .limit(limit)
                .map(GoodsReserveDto::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * 굿즈 예약 전체 개수
     */
    public long countGoodsReserves() {
        return goodsReserveRepository.count();
    }

    /** ★ 삭제 기능 추가 */
    public void deleteById(Long id) {
        if (!goodsReserveRepository.existsById(id)) {
            throw new IllegalArgumentException("삭제할 장례 예약이 존재하지 않습니다. id=" + id);
        }
        goodsReserveRepository.deleteById(id);
        log.info("[관리자] 장례 예약 삭제 완료: id={}", id);
    }

    public List<GoodsReserveDto> searchGoods(String keyword) {
        return goodsReserveRepository.findAll()
                .stream()
                .filter(g ->
                        g.getOwnerName().contains(keyword) ||
                                g.getProduct().contains(keyword) ||
                                g.getUser().getName().contains(keyword)
                )
                .map(GoodsReserveDto::fromEntity)
                .collect(Collectors.toList());
    }

}
