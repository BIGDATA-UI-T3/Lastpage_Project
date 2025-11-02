package com.example.demo.Domain.Common.Service;

import com.example.demo.Domain.Common.Dto.ReserveDto;
import com.example.demo.Domain.Common.Entity.GoodsReserve;
import com.example.demo.Domain.Common.Entity.User;
import com.example.demo.Repository.GoodsReserveRepository;
import com.example.demo.Repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@AllArgsConstructor
public class GoodsReserveService {

    private final GoodsReserveRepository repository;
    private final UserRepository userRepository;

    @Transactional
    public GoodsReserve saveReservation(ReserveDto dto, String username) {

        // 1. username으로 User 엔티티를 찾습니다.
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("예약 중 사용자를 찾을 수 없습니다: " + username));

        // 2. DTO -> Entity 변환 (기존 로직)
        String materialsStr = String.join(",", dto.getMaterials());
        Integer quantityToSave = dto.getQuantity();
        if (quantityToSave == null || quantityToSave < 1) {
            quantityToSave = 1;
        }

        GoodsReserve entity = GoodsReserve.builder()
                .user(user) // 3. 찾은 User 엔티D티를 예약 정보에 설정
                .ownerName(dto.getOwnerName())
                .ownerPhone(dto.getOwnerPhone())
                .ownerEmail(dto.getOwnerEmail())
                .ownerAddr(dto.getOwnerAddr())
                .petName(dto.getPetName())
                .petType(dto.getPetType())
                .petBreed(dto.getPetBreed())
                .petWeight(dto.getPetWeight())
                .memo(dto.getMemo())
                .materials(materialsStr)
                .product(dto.getProduct())
                .metalColor(dto.getMetalColor())
                .chainLength(dto.getChainLength())
                .ringSize(dto.getRingSize())
                .quantity(quantityToSave)
                .engravingText(dto.getEngravingText())

                // [오타 수정 완료] 4. getEngFont() -> getEngravingFont()
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

        // 5. DB에 저장
        return repository.save(entity);
    }

    // '모든' 예약을 가져옵니다. (관리자용)
    @Transactional(readOnly = true)
    public List<GoodsReserve> getAllGoodsReservations() {
        return repository.findAll();
    }

    // '특정 사용자'의 예약 목록만 가져오는 메서드
    @Transactional(readOnly = true)
    public List<GoodsReserve> getAllGoodsReservationsByUsername(String username) {
        return repository.findByUserUsername(username);
    }

    // 예약 삭제
    @Transactional
    public void deleteReservation(Long id) {
        GoodsReserve reservation = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 예약을 찾을 수 없습니다. id=" + id));

        repository.delete(reservation);
    }
}