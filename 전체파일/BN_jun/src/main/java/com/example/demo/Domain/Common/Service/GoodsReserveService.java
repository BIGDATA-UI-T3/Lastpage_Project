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
@Transactional
public class GoodsReserveService {

    private final GoodsReserveRepository repository;
    private final UserRepository userRepository;

    public GoodsReserve saveReservation(ReserveDto dto, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("예약 중 사용자를 찾을 수 없습니다: " + username));
        String materialsStr = String.join(",", dto.getMaterials());
        Integer quantityToSave = dto.getQuantity();
        if (quantityToSave == null || quantityToSave < 1) {
            quantityToSave = 1;
        }
        GoodsReserve entity = GoodsReserve.builder()
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
                .materials(materialsStr)
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
    public GoodsReserve getReservationById(Long id) {
        return repository.findByIdWithUser(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 예약을 찾을 수 없습니다. id=" + id));
    }

    // 예약 수정(UPDATE) 로직
    public GoodsReserve updateReservation(Long id, ReserveDto dto, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + username));
        GoodsReserve entity = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 예약을 찾을 수 없습니다. id=" + id));
        if (!entity.getUser().getId().equals(user.getId())) {
            throw new IllegalStateException("이 예약을 수정할 권한이 없습니다.");
        }
        String materialsStr = String.join(",", dto.getMaterials());
        Integer quantityToSave = dto.getQuantity();
        if (quantityToSave == null || quantityToSave < 1) {
            quantityToSave = 1;
        }
        entity.setOwnerName(dto.getOwnerName());
        entity.setOwnerPhone(dto.getOwnerPhone());
        entity.setOwnerEmail(dto.getOwnerEmail());
        entity.setOwnerAddr(dto.getOwnerAddr());
        entity.setPetName(dto.getPetName());
        entity.setPetType(dto.getPetType());
        entity.setPetBreed(dto.getPetBreed());
        entity.setPetWeight(dto.getPetWeight());
        entity.setMemo(dto.getMemo());
        entity.setMaterials(materialsStr);
        entity.setProduct(dto.getProduct());
        entity.setMetalColor(dto.getMetalColor());
        entity.setChainLength(dto.getChainLength());
        entity.setRingSize(dto.getRingSize());
        entity.setQuantity(quantityToSave);
        entity.setEngravingText(dto.getEngravingText());
        entity.setEngravingFont(dto.getEngravingFont());
        entity.setOptionsMemo(dto.getOptionsMemo());
        entity.setShipMethod(dto.getShipMethod());
        entity.setTargetDate(dto.getTargetDate());
        entity.setIsExpress(dto.getIsExpress());
        entity.setKitAddr(dto.getKitAddr());
        entity.setKitDate(dto.getKitDate());
        entity.setKitTime(dto.getKitTime());
        entity.setVisitDate(dto.getVisitDate());
        entity.setVisitTime(dto.getVisitTime());
        entity.setTrackingInfo(dto.getTrackingInfo());
        return repository.save(entity);
    }

    // 모든 예약 조회
    @Transactional(readOnly = true)
    public List<GoodsReserve> getAllGoodsReservations() {
        return repository.findAll();
    }

    // 특정 사용자의 예약 목록 조회
    @Transactional(readOnly = true)
    public List<GoodsReserve> getAllGoodsReservationsByUsername(String username) {
        return repository.findByUserUsername(username);
    }

    // 예약 삭제
    public void deleteReservation(Long id) {
        GoodsReserve reservation = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 예약을 찾을 수 없습니다. id=" + id));
        repository.delete(reservation);
    }
}