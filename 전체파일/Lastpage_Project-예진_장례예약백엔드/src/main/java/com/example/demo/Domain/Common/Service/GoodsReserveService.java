package com.example.demo.Domain.Common.Service;

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
        GoodsReserve entity = GoodsReserve.builder()
                .ownerName(dto.getOwnerName())
                .ownerPhone(dto.getOwnerPhone())
                .ownerEmail(dto.getOwnerEmail())
                .ownerAddr(dto.getOwnerAddr())
                .petName(dto.getPetName())
                .petType(dto.getPetType())
                .petBreed(dto.getPetBreed())
                .petWeight(dto.getPetWeight())

                .passedAt(dto.getPassedAt())
                .place(dto.getPlace())
                .goodsDate(dto.getGoodsDate())
                .type(dto.getType())
                .ash(dto.getAsh())
                .pickup(dto.getPickup())
                .pickupAddr(dto.getPickupAddr())
                .pickupTime(dto.getPickupTime())
                .time(dto.getTime())

                .memo(dto.getMemo())
                .build();

        return repository.save(entity);
    }
}
