package com.example.demo.Domain.Common.Service;

import com.example.demo.Domain.Common.Dto.ReserveDto;
import com.example.demo.Domain.Common.Entity.GoodsReserve;
import com.example.demo.Domain.Common.Entity.User; // [추가]
import com.example.demo.Repository.GoodsReserveRepository;
import com.example.demo.Repository.UserRepository; // [추가]
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@AllArgsConstructor // [수정] final 필드 모두 주입
public class GoodsReserveService {

    private final GoodsReserveRepository repository;
    private final UserRepository userRepository; // [추가] 예약을 한 사용자를 찾기 위해

    @Transactional // [수정] saveReservation 메서드에 @Transactional 추가
    public GoodsReserve saveReservation(ReserveDto dto, String username) { // [수정] username 파라미터 추가

        // [추가] username으로 User 엔티티를 찾습니다.
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("예약 중 사용자를 찾을 수 없습니다: " + username));

        // ... (기존 materialsStr, quantityToSave 로직 동일) ...
        String materialsStr = String.join(",", dto.getMaterials());
        Integer quantityToSave = dto.getQuantity();
        if (quantityToSave == null || quantityToSave < 1) {
            quantityToSave = 1;
        }

        GoodsReserve entity = GoodsReserve.builder()
                // 기존 매핑
                .ownerName(dto.getOwnerName())
                .ownerPhone(dto.getOwnerPhone())
                .ownerEmail(dto.getOwnerEmail())
                .ownerAddr(dto.getOwnerAddr())
                .petName(dto.getPetName())
                .petType(dto.getPetType())
                .petBreed(dto.getPetBreed())
                .petWeight(dto.getPetWeight())
                .memo(dto.getMemo())

                // 추가된 필드 매핑
                .materials(materialsStr) // 변환된 문자열 저장
                .product(dto.getProduct())
                .metalColor(dto.getMetalColor())
                .chainLength(dto.getChainLength())
                .ringSize(dto.getRingSize())
                .quantity(quantityToSave)
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

        return repository.save(entity);
    }

    @Transactional(readOnly = true)
    public List<GoodsReserve> getAllGoodsReservations() {
        return repository.findAll();
    }

    // [추가] '특정 사용자'의 예약 목록만 가져오는 메서드
    @Transactional(readOnly = true)
    public List<GoodsReserve> getAllGoodsReservationsByUsername(String username) {
        return repository.findByUserUsername(username);
    }

    /**
     * ID를 기반으로 예약을 삭제합니다. (수정 필요 없음)
     */
    @Transactional
    public void deleteReservation(Long id) {
        GoodsReserve reservation = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 예약을 찾을 수 없습니다. id=" + id));

        // [추정] TODO: 여기에 예약 삭제 전, 본인 확인 로직 추가 필요 (보안 강화)

        repository.delete(reservation);
    }
}