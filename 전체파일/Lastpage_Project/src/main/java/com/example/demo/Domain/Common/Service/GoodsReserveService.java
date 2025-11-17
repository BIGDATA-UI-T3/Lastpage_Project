package com.example.demo.Domain.Common.Service;

// [수정] Import 경로 확인

import com.example.demo.Domain.Common.Dto.GoodsReserveDto;
import com.example.demo.Domain.Common.Dto.PsyReserveDto;
import com.example.demo.Domain.Common.Entity.GoodsReserve;
import com.example.demo.Domain.Common.Entity.PsyReserve;
import com.example.demo.Domain.Common.Entity.Signup;
import com.example.demo.Repository.GoodsReserveRepository;
import com.example.demo.Repository.SignupRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.AccessDeniedException;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class GoodsReserveService {

    private final GoodsReserveRepository goodsReserveRepository;
    private final SignupRepository signupRepository; // 회원정보 Repository

    // ... (saveReservation, getReservationById, updateReservation 메서드는 동일) ...
    // (이전 코드들 생략)

    public GoodsReserveDto saveReservation(GoodsReserveDto dto) {
        Signup user = signupRepository.findById(dto.getUserSeq())
                .orElseThrow(() -> new IllegalArgumentException("해당 회원(user_seq)을 찾을 수 없습니다: " + dto.getUserSeq()));

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
                .materials(String.join(",", dto.getMaterials())) // List<String> -> "A,B,C"
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
//         
        log.info("[굿즈 예약 등록 완료!] user_seq={}",user.getUserSeq());
        return toDto(saved);
    }

    /** ------------------------------
     *  굿즈 예약 수정(본인만 가능)
     * ------------------------------ */
   

    @Transactional
    public GoodsReserveDto updateReserve(Long id, GoodsReserveDto updated, String userSeq) {
        GoodsReserve existing = goodsReserveRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("예약을 찾을 수 없습니다. ID=" + id));

        // 본인 예약인지 확인
        if (!existing.getUser().getUserSeq().equals(userSeq)) {
            throw new SecurityException("본인의 예약만 수정할 수 있습니다.");
        }

//        boolean sameSlot = existing.getConsultDate().equals(updated.getConsultDate())
//                && existing.getTime().equals(updated.getTime());

//        if (!sameSlot && repository.existsByConsultDateAndTime(updated.getConsultDate(), updated.getTime())) {
//            throw new IllegalStateException("이미 예약된 시간입니다.");
//        }
        existing.setOwnerName(updated.getOwnerName());
        existing.setOwnerPhone(updated.getOwnerPhone());
        existing.setOwnerEmail(updated.getOwnerEmail());
        existing.setOwnerAddr(updated.getOwnerAddr());
        existing.setPetName(updated.getPetName());
        existing.setPetType(updated.getPetType());
        existing.setPetBreed(updated.getPetBreed());
        existing.setPetWeight(updated.getPetWeight());
        existing.setMemo(updated.getMemo());
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
        
        log.info("[굿즈예약 수정 완료] ID={}, user_seq={}",id, userSeq);
        return toDto(saved);
        
    }
    /** ------------------------------
     *  user_seq 기준 다건 예약 조회 (마이페이지용)
     * ------------------------------ */
    public List<GoodsReserveDto> findAllByUserSeq(String userSeq) {
        List<GoodsReserve> list = goodsReserveRepository.findAllByUser_UserSeq(userSeq);
        if (list.isEmpty()) {
            log.info("[굿즈 예약 조회] 해당 user_seq로 예약 없음: {}", userSeq);
            return Collections.emptyList();
        }
        log.info("[굿즈 예약 조회 완료] user_seq={}, {}건", userSeq, list.size());
        return list.stream().map(this::toDto).collect(Collectors.toList());
    }


    /** ------------------------------
     *  ID 기준 단건 예약 조회 (수정폼 진입용)
     * ------------------------------ */
    public GoodsReserveDto findById(Long id) {
        return goodsReserveRepository.findById(id)
                .map(this::toDto)
                .orElse(null);
    }


    /** ------------------------------
     *  전체 예약 조회 (관리자용)
     * ------------------------------ */
    public List<GoodsReserveDto> findAll() {
        return goodsReserveRepository.findAll()
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public boolean deleteReserve(Long id, String userSeq) {
        GoodsReserve existing = goodsReserveRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 예약입니다. ID=" + id));

        if (existing.getUser() == null || !existing.getUser().getUserSeq().equals(userSeq)) {
            log.info("ID: {}, user_seq={}", id, userSeq);
            throw new SecurityException("본인의 예약만 삭제할 수 있습니다.");
        }

        goodsReserveRepository.delete(existing);
        goodsReserveRepository.flush();  // 강제로 DB 반영
        log.info("[상담예약 삭제 완료] ID={}, user_seq={}", id, userSeq);
        return true;
    }



//    @Transactional(readOnly = true)
//    public GoodsReserve getReservationById(Long id) {
//        return goodsReserveRepository.findById(id)
//                .orElseThrow(() -> new RuntimeException("예약을 찾을 수 없습니다. ID: " + id));
//    }
//
//    public GoodsReserve updateReservation(Long id, GoodsReserveDto dto, String username) throws AccessDeniedException {
//        GoodsReserve existing = getReservationById(id);
//        Signup user = signupRepository.findById(dto.getUserSeq())
//                .orElseThrow(() -> new NoSuchElementException("사용자를 찾을 수 없습니다: " +dto.getUserSeq() ));
//
//        if (!existing.getUser().getUserSeq().equals(user.getUserSeq())) {
//            throw new AccessDeniedException("예약을 수정할 권한이 없습니다.");
//        }
//
//        existing.setOwnerName(dto.getOwnerName());
//        existing.setOwnerPhone(dto.getOwnerPhone());
//        existing.setOwnerEmail(dto.getOwnerEmail());
//        existing.setOwnerAddr(dto.getOwnerAddr());
//        existing.setPetName(dto.getPetName());
//        existing.setPetType(dto.getPetType());
//        existing.setPetBreed(dto.getPetBreed());
//        existing.setPetWeight(dto.getPetWeight());
//        existing.setMemo(dto.getMemo());
//        existing.setMaterials(String.join(",", dto.getMaterials()));
//        existing.setProduct(dto.getProduct());
//        existing.setMetalColor(dto.getMetalColor());
//        existing.setChainLength(dto.getChainLength());
//        existing.setRingSize(dto.getRingSize());
//        existing.setQuantity(dto.getQuantity());
//        existing.setEngravingText(dto.getEngravingText());
//        existing.setEngravingFont(dto.getEngravingFont());
//        existing.setOptionsMemo(dto.getOptionsMemo());
//        existing.setShipMethod(dto.getShipMethod());
//        existing.setTargetDate(dto.getTargetDate());
//        existing.setIsExpress(dto.getIsExpress());
//        existing.setKitAddr(dto.getKitAddr());
//        existing.setKitDate(dto.getKitDate());
//        existing.setKitTime(dto.getKitTime());
//        existing.setVisitDate(dto.getVisitDate());
//        existing.setVisitTime(dto.getVisitTime());
//        existing.setTrackingInfo(dto.getTrackingInfo());
//
//        return existing;
//    }
//
//    public void deleteReservation(Long id, String username) throws AccessDeniedException {
//        if (username == null) {
//            throw new AccessDeniedException("로그인이 필요합니다.");
//        }
//
//        GoodsReserve reservation = goodsReserveRepository.findById(id)
//                .orElseThrow(() -> new RuntimeException("예약을 찾을 수 없습니다. ID: " + id));
//
//        if (!reservation.getUser().getUserSeq().equals(username)) {
//            throw new AccessDeniedException("예약을 삭제할 권한이 없습니다.");
//        }
//
//        goodsReserveRepository.deleteById(id);
//    }
//
//    // ▼▼▼ [수정] 이 메서드를 간단하게 변경 ▼▼▼
//    @Transactional(readOnly = true)
//    public List<GoodsReserve> getAllGoodsReservationsByUserSeq(String userSeq) {
//        // Repository의 기능을 바로 호출합니다. (User를 찾는 과정 불필요)
//        return goodsReserveRepository.findByUser_UserSeq(userSeq);
//    }
public long countAll() {
    return goodsReserveRepository.count();
}

    public List<GoodsReserveDto> findRecent5() {
        return goodsReserveRepository.findTop5ByOrderByCreated_atDesc()
                .stream()
                .map(GoodsReserveDto::fromEntity)
                .toList();
    }


    private GoodsReserveDto toDto(GoodsReserve entity) {
        GoodsReserveDto dto = new GoodsReserveDto();

        dto.setId(entity.getId());

        // FK - user_seq
        if (entity.getUser() != null) {
            dto.setUserSeq(entity.getUser().getUserSeq());
        }

        // === 기본 정보 ===
        dto.setOwnerName(entity.getOwnerName());
        dto.setOwnerPhone(entity.getOwnerPhone());
        dto.setOwnerEmail(entity.getOwnerEmail());
        dto.setOwnerAddr(entity.getOwnerAddr());
        dto.setPetName(entity.getPetName());
        dto.setPetType(entity.getPetType());
        dto.setPetBreed(entity.getPetBreed());
        dto.setPetWeight(entity.getPetWeight());
        dto.setMemo(entity.getMemo());

        // === Step 1 추가 ===
        dto.setMaterials(Collections.singletonList(entity.getMaterials()));

        // === Step 2 추가 ===
        dto.setProduct(entity.getProduct());
        dto.setMetalColor(entity.getMetalColor());
        dto.setChainLength(entity.getChainLength());
        dto.setRingSize(entity.getRingSize());
        dto.setQuantity(entity.getQuantity());
        dto.setEngravingText(entity.getEngravingText());
        dto.setEngravingFont(entity.getEngravingFont());
        dto.setOptionsMemo(entity.getOptionsMemo());

        // === Step 3 추가 ===
        dto.setShipMethod(entity.getShipMethod());
        dto.setTargetDate(entity.getTargetDate());
        dto.setIsExpress(entity.getIsExpress());
        dto.setKitAddr(entity.getKitAddr());
        dto.setKitDate(entity.getKitDate());
        dto.setKitTime(entity.getKitTime());
        dto.setVisitDate(entity.getVisitDate());
        dto.setVisitTime(entity.getVisitTime());
        dto.setTrackingInfo(entity.getTrackingInfo());

        return dto;
    }



    // ▲▲▲
}