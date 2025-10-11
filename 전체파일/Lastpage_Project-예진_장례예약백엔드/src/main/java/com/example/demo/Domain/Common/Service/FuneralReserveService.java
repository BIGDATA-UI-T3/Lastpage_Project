package com.example.demo.Domain.Common.Service;

import com.example.demo.Domain.Common.Dto.ReserveDto;
import com.example.demo.Domain.Common.Entity.FuneralReserve;
import com.example.demo.Repository.FuneralReserveRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class FuneralReserveService {

    private final FuneralReserveRepository repository;

    public FuneralReserve saveReservation(ReserveDto dto) {
        FuneralReserve entity = FuneralReserve.builder()
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
                .funeralDate(dto.getFuneralDate())
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
