package com.example.demo.Domain.Common.Service;

import com.example.demo.Domain.Common.Dto.ReserveDto;
import com.example.demo.Domain.Common.Entity.GoodsReserve;
import com.example.demo.Repository.GoodsReserveRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@AllArgsConstructor
public class GoodsReserveService {

    private final GoodsReserveRepository repository;

    public GoodsReserve saveReservation(ReserveDto dto) {
        // JS의 배열(materials)을 DB에 저장하기 위해 문자열로 변환
        String materialsStr = String.join(",", dto.getMaterials());

        Integer quantityToSave = dto.getQuantity();
        if (quantityToSave == null || quantityToSave < 1) {
            quantityToSave = 1; // 널이거나 1보다 작으면 1로 강제 설정
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

    // 데이터베이스에 저장된 모든 굿즈 예약 목록을 조회하는 기능, @return GoodsReserve 객체들이 담긴 List임
    @Transactional(readOnly = true) // 데이터를 읽기만 하므로 readOnly = true 옵션 추가 (성능 향상)
    public List<GoodsReserve> getAllGoodsReservations() {
        return repository.findAll(); // JpaRepository가 기본으로 제공하는 '전체 조회' 기능
    }

    /**
     * ID를 기반으로 예약을 삭제합니다.
     * @param id 삭제할 예약의 ID
     */
    @Transactional // 데이터를 삭제(변경)하므로 @Transactional 추가
    public void deleteReservation(Long id) {
        // ID로 예약을 찾습니다.
        GoodsReserve reservation = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 예약을 찾을 수 없습니다. id=" + id));

        // 찾은 예약을 삭제합니다.
        repository.delete(reservation);

        // 또는 repository.deleteById(id); 를 바로 사용해도 됩니다.
    }
}