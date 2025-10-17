//package com.example.demo.Domain.Common.Service;
//
//import com.example.demo.Domain.Common.Dto.ReserveDto;
//import com.example.demo.Domain.Common.Entity.GoodsReserve;
//import com.example.demo.Repository.GoodsReserveRepository;
//import lombok.AllArgsConstructor;
//import org.springframework.stereotype.Service;
//
//@Service
//@AllArgsConstructor
//public class GoodsReserveService {
//
//    private final GoodsReserveRepository repository;
//
//    public GoodsReserve saveReservation(ReserveDto dto) {
//        GoodsReserve entity = GoodsReserve.builder()
//                .ownerName(dto.getOwnerName())
//                .ownerPhone(dto.getOwnerPhone())
//                .ownerEmail(dto.getOwnerEmail())
//                .ownerAddr(dto.getOwnerAddr())
//                .petName(dto.getPetName())
//                .petType(dto.getPetType())
//                .petBreed(dto.getPetBreed())
//                .petWeight(dto.getPetWeight())
//
//                .passedAt(dto.getPassedAt())
//                .place(dto.getPlace())
//                .goodsDate(dto.getGoodsDate())
//                .type(dto.getType())
//                .ash(dto.getAsh())
//                .pickup(dto.getPickup())
//                .pickupAddr(dto.getPickupAddr())
//                .pickupTime(dto.getPickupTime())
//                .time(dto.getTime())
//
//                .memo(dto.getMemo())
//                .build();
//
//        return repository.save(entity);
//    }
//}


package com.example.demo.Domain.Common.Service;

// ... imports

import com.example.demo.Domain.Common.Dto.ReserveDto;
import com.example.demo.Domain.Common.Entity.GoodsReserve;
import com.example.demo.Repository.GoodsReserveRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class GoodsReserveService {

    private final GoodsReserveRepository repository;

    public GoodsReserve saveReservation(ReserveDto dto) {
        // JS의 배열(materials)을 DB에 저장하기 위해 문자열로 변환
        String materialsStr = String.join(",", dto.getMaterials());

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

        return repository.save(entity);
    }
}