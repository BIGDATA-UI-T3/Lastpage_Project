package com.example.demo.Domain.Common.Service;

import com.example.demo.Domain.Common.Dto.PsyReserveDto;
import com.example.demo.Domain.Common.Entity.PsyReserve;
import com.example.demo.Repository.PsyReserveRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class PsyReserveService {

    private  final PsyReserveRepository repository;

    public PsyReserve saveReservation(PsyReserveDto dto) {
        PsyReserve entity = PsyReserve.builder()
                .name(dto.getName())
                .birth(dto.getBirth())
                .gender(dto.getGender())
                .phone(dto.getPhone())
                .email(dto.getEmail())
                .address(dto.getAddress())
                .consultDate(dto.getConsultDate())
                .time(dto.getTime())
                .counselor(dto.getCounselor())
                .memo(dto.getMemo())
                .build();

        return repository.save(entity);
    }
}
