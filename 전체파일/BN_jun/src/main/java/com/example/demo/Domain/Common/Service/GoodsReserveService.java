package com.example.demo.Domain.Common.Service;

// [수정] Import 경로 확인
import com.example.demo.Domain.Common.Dto.GoodsReserveDto;
import com.example.demo.Domain.Common.Entity.GoodsReserve;
import com.example.demo.Domain.Common.Entity.User;
import com.example.demo.Repository.GoodsReserveRepository;
import com.example.demo.Repository.UserRepository;
//
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.AccessDeniedException;
import java.util.List; // 👈 [추가]
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
@Transactional
public class GoodsReserveService {

    private final GoodsReserveRepository goodsReserveRepository;
    private final UserRepository userRepository;

    // ... (saveReservation, getReservationById, updateReservation 메서드는 동일) ...
    // (이전 코드들 생략)

    public GoodsReserve saveReservation(GoodsReserveDto dto, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new NoSuchElementException("사용자를 찾을 수 없습니다: " + username));

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

        return goodsReserveRepository.save(reserve);
    }

    @Transactional(readOnly = true)
    public GoodsReserve getReservationById(Long id) {
        return goodsReserveRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("예약을 찾을 수 없습니다. ID: " + id));
    }

    public GoodsReserve updateReservation(Long id, GoodsReserveDto dto, String username) throws AccessDeniedException {
        GoodsReserve existing = getReservationById(id);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new NoSuchElementException("사용자를 찾을 수 없습니다: " + username));

        if (!existing.getUser().getUsername().equals(user.getUsername())) {
            throw new AccessDeniedException("예약을 수정할 권한이 없습니다.");
        }

        existing.setOwnerName(dto.getOwnerName());
        existing.setOwnerPhone(dto.getOwnerPhone());
        existing.setOwnerEmail(dto.getOwnerEmail());
        existing.setOwnerAddr(dto.getOwnerAddr());
        existing.setPetName(dto.getPetName());
        existing.setPetType(dto.getPetType());
        existing.setPetBreed(dto.getPetBreed());
        existing.setPetWeight(dto.getPetWeight());
        existing.setMemo(dto.getMemo());
        existing.setMaterials(String.join(",", dto.getMaterials()));
        existing.setProduct(dto.getProduct());
        existing.setMetalColor(dto.getMetalColor());
        existing.setChainLength(dto.getChainLength());
        existing.setRingSize(dto.getRingSize());
        existing.setQuantity(dto.getQuantity());
        existing.setEngravingText(dto.getEngravingText());
        existing.setEngravingFont(dto.getEngravingFont());
        existing.setOptionsMemo(dto.getOptionsMemo());
        existing.setShipMethod(dto.getShipMethod());
        existing.setTargetDate(dto.getTargetDate());
        existing.setIsExpress(dto.getIsExpress());
        existing.setKitAddr(dto.getKitAddr());
        existing.setKitDate(dto.getKitDate());
        existing.setKitTime(dto.getKitTime());
        existing.setVisitDate(dto.getVisitDate());
        existing.setVisitTime(dto.getVisitTime());
        existing.setTrackingInfo(dto.getTrackingInfo());

        return existing;
    }

    public void deleteReservation(Long id, String username) throws AccessDeniedException {
        if (username == null) {
            throw new AccessDeniedException("로그인이 필요합니다.");
        }

        GoodsReserve reservation = goodsReserveRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("예약을 찾을 수 없습니다. ID: " + id));

        if (!reservation.getUser().getUsername().equals(username)) {
            throw new AccessDeniedException("예약을 삭제할 권한이 없습니다.");
        }

        goodsReserveRepository.deleteById(id);
    }

    // ▼▼▼ [수정] 이 메서드를 간단하게 변경 ▼▼▼
    @Transactional(readOnly = true)
    public List<GoodsReserve> getAllGoodsReservationsByUsername(String username) {
        // Repository의 기능을 바로 호출합니다. (User를 찾는 과정 불필요)
        return goodsReserveRepository.findByUserUsername(username);
    }
    // ▲▲▲
}