package com.example.demo.Domain.Common.Service;

import com.example.demo.Domain.Common.Dto.GoodsReserveDto;
import com.example.demo.Domain.Common.Entity.GoodsReserve;
import com.example.demo.Domain.Common.Entity.PaymentStatus;
import com.example.demo.Domain.Common.Entity.Signup;
import com.example.demo.Repository.GoodsReserveRepository;
import com.example.demo.Repository.SignupRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class GoodsReserveService {

    private final GoodsReserveRepository goodsReserveRepository;
    private final SignupRepository signupRepository;

    /* =========================================================
     *  [1] 관리자/사용자 겸용 단건 조회
     *  isAdmin=true → 소유권 검사 생략
     * ========================================================= */
    @Transactional(readOnly = true)
    public GoodsReserveDto findByIdForAdminOrUser(Long id, String requestUserSeq, boolean isAdmin) {

        GoodsReserve entity = goodsReserveRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("예약을 찾을 수 없습니다. ID=" + id));

        if (!isAdmin) {
            if (entity.getUser() == null ||
                    !entity.getUser().getUserSeq().equals(requestUserSeq)) {
                throw new SecurityException("본인의 예약만 조회할 수 있습니다.");
            }
        }

        return toDto(entity);
    }

    /* =========================================================
     *  [2] 관리자/사용자 겸용 수정
     * ========================================================= */
    @Transactional
    public GoodsReserveDto updateForAdminOrUser(Long id, GoodsReserveDto updated,
                                                String requestUserSeq, boolean isAdmin) {

        GoodsReserve existing = goodsReserveRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("예약을 찾을 수 없습니다. ID=" + id));

        if (!isAdmin) {
            if (!existing.getUser().getUserSeq().equals(requestUserSeq)) {
                throw new SecurityException("본인의 예약만 수정할 수 있습니다.");
            }
        }

        // ===== 수정 필드 적용 =====
        existing.setOwnerName(updated.getOwnerName());
        existing.setOwnerPhone(updated.getOwnerPhone());
        existing.setOwnerEmail(updated.getOwnerEmail());
        existing.setOwnerAddr(updated.getOwnerAddr());
        existing.setPetName(updated.getPetName());
        existing.setPetType(updated.getPetType());
        existing.setPetBreed(updated.getPetBreed());
        existing.setPetWeight(updated.getPetWeight());
        existing.setMemo(updated.getMemo());

        // 리스트 → 문자열 join
        existing.setMaterials(String.join(",", updated.getMaterials()));

        existing.setProduct(updated.getProduct());
        existing.setMetalColor(updated.getMetalColor());
        existing.setChainLength(updated.getChainLength());
        existing.setRingSize(updated.getRingSize());
        existing.setQuantity(updated.getQuantity());
        existing.setEngravingText(updated.getEngravingText());
        existing.setEngravingFont(updated.getEngravingFont());
        existing.setOptionsMemo(updated.getOptionsMemo());
        existing.setShipMethod(updated.getShipMethod());
        existing.setTargetDate(updated.getTargetDate());
        existing.setIsExpress(updated.getIsExpress());
        existing.setKitAddr(updated.getKitAddr());
        existing.setKitDate(updated.getKitDate());
        existing.setKitTime(updated.getKitTime());
        existing.setVisitDate(updated.getVisitDate());
        existing.setVisitTime(updated.getVisitTime());
        existing.setTrackingInfo(updated.getTrackingInfo());

        GoodsReserve saved = goodsReserveRepository.save(existing);

        log.info("[{} 굿즈 예약 수정] ID={}, 요청자={}, 소유자={}",
                isAdmin ? "ADMIN" : "USER",
                id, requestUserSeq, existing.getUser().getUserSeq());

        return toDto(saved);
    }

    /* =========================================================
     *  [3] 관리자/사용자 겸용 삭제
     * ========================================================= */
    @Transactional
    public boolean deleteForAdminOrUser(Long id, String requestUserSeq, boolean isAdmin) {

        GoodsReserve existing = goodsReserveRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 예약입니다. ID=" + id));

        if (!isAdmin) {
            if (!existing.getUser().getUserSeq().equals(requestUserSeq)) {
                throw new SecurityException("본인의 예약만 삭제할 수 있습니다.");
            }
        }

        goodsReserveRepository.delete(existing);
        goodsReserveRepository.flush();

        log.info("[{} 굿즈 예약 삭제] ID={}, 요청자={}, 소유자={}",
                isAdmin ? "ADMIN" : "USER",
                id, requestUserSeq, existing.getUser().getUserSeq());

        return true;
    }

    /* =========================================================
     *  기존 사용자 전용 로직 (컨트롤러 호환 위해 유지)
     * ========================================================= */

    public GoodsReserveDto saveReservation(GoodsReserveDto dto) {
        Signup user = signupRepository.findById(dto.getUserSeq())
                .orElseThrow(() -> new IllegalArgumentException("해당 회원(user_seq)을 찾을 수 없습니다. " + dto.getUserSeq()));

        GoodsReserve reserve = GoodsReserve.builder()
                .user(user)
                .ownerName(dto.getOwnerName())
                .ownerPhone(dto.getOwnerPhone())
                .ownerEmail(dto.getOwnerEmail())
                .ownerAddr(dto.getOwnerAddr())
                .petName(dto.getPetName())
                .petType(dto.getPetType())
                .petBreed(dto.getPetBreed())
                .petWeight(dto.getPetWeight())
                .memo(dto.getMemo())
                .materials(String.join(",", dto.getMaterials()))
                .product(dto.getProduct())
                .metalColor(dto.getMetalColor())
                .chainLength(dto.getChainLength())
                .ringSize(dto.getRingSize())
                .quantity(dto.getQuantity())
                .engravingText(dto.getEngravingText())
                .engravingFont(dto.getEngravingFont())
                .optionsMemo(dto.getOptionsMemo())
                .shipMethod(dto.getShipMethod())
                .targetDate(dto.getTargetDate())
                .isExpress(dto.getIsExpress())
                .kitAddr(dto.getKitAddr())
                .kitDate(dto.getKitDate())
                .kitTime(dto.getKitTime())
                .visitDate(dto.getVisitDate())
                .visitTime(dto.getVisitTime())
                .trackingInfo(dto.getTrackingInfo())
                .build();

        GoodsReserve saved = goodsReserveRepository.save(reserve);

        log.info("[굿즈 예약 등록 완료] user_seq={}", dto.getUserSeq());
        return toDto(saved);
    }

    /* 사용자 전용 수정 → 공용 수정 래퍼 */
    public GoodsReserveDto updateReserve(Long id, GoodsReserveDto dto, String userSeq) {
        return updateForAdminOrUser(id, dto, userSeq, false);
    }

    /* 사용자 전용 삭제 → 공용 삭제 래퍼 */
    public boolean deleteReserve(Long id, String userSeq) {
        return deleteForAdminOrUser(id, userSeq, false);
    }

    /* --------------------------------------------------------- */

    public GoodsReserveDto findById(Long id) {
        return goodsReserveRepository.findById(id)
                .map(this::toDto)
                .orElse(null);
    }

    public List<GoodsReserveDto> findAllByUserSeq(String userSeq) {
        return goodsReserveRepository.findAllByUser_UserSeq(userSeq)
                .stream().map(this::toDto).toList();
    }

    public List<GoodsReserveDto> findAll() {
        return goodsReserveRepository.findAll()
                .stream().map(this::toDto).toList();
    }

    @Transactional(readOnly = true)
    public GoodsReserve findEntityById(Long id) {
        return goodsReserveRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("굿즈 예약을 찾을 수 없습니다. ID=" + id));
    }

    @Transactional
    public void markPaid(Long reserveId) {
        GoodsReserve reserve = goodsReserveRepository.findById(reserveId)
                .orElseThrow(() -> new IllegalArgumentException("예약을 찾을 수 없습니다."));

        reserve.setPaymentStatus(PaymentStatus.PAID);
    }
    @Transactional
    public void updateStatus(Long reserveId, String status) {
        GoodsReserve reserve = goodsReserveRepository.findById(reserveId)
                .orElseThrow(() -> new IllegalArgumentException("예약 정보를 찾을 수 없습니다."));

        reserve.setPaymentStatus(PaymentStatus.valueOf(status));   // status = "PAID"
        goodsReserveRepository.save(reserve);
    }



    /* =========================================================
     * DTO 변환
     * ========================================================= */
    private GoodsReserveDto toDto(GoodsReserve entity) {
        GoodsReserveDto dto = new GoodsReserveDto();
        dto.setId(entity.getId());

        if (entity.getUser() != null)
            dto.setUserSeq(entity.getUser().getUserSeq());

        dto.setOwnerName(entity.getOwnerName());
        dto.setOwnerPhone(entity.getOwnerPhone());
        dto.setOwnerEmail(entity.getOwnerEmail());
        dto.setOwnerAddr(entity.getOwnerAddr());
        dto.setPetName(entity.getPetName());
        dto.setPetType(entity.getPetType());
        dto.setPetBreed(entity.getPetBreed());
        dto.setPetWeight(entity.getPetWeight());
        dto.setMemo(entity.getMemo());

        // 문자열 → List 변환
        dto.setMaterials(Arrays.asList(entity.getMaterials().split(",")));

        dto.setProduct(entity.getProduct());
        dto.setMetalColor(entity.getMetalColor());
        dto.setChainLength(entity.getChainLength());
        dto.setRingSize(entity.getRingSize());
        dto.setQuantity(entity.getQuantity());
        dto.setEngravingText(entity.getEngravingText());
        dto.setEngravingFont(entity.getEngravingFont());
        dto.setOptionsMemo(entity.getOptionsMemo());

        dto.setShipMethod(entity.getShipMethod());
        dto.setTargetDate(entity.getTargetDate());
        dto.setIsExpress(entity.getIsExpress());
        dto.setKitAddr(entity.getKitAddr());
        dto.setKitDate(entity.getKitDate());
        dto.setKitTime(entity.getKitTime());
        dto.setVisitDate(entity.getVisitDate());
        dto.setVisitTime(entity.getVisitTime());
        dto.setTrackingInfo(entity.getTrackingInfo());
        dto.setPaymentStatus(entity.getPaymentStatus());

        return dto;
    }

}
