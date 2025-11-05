package com.example.demo.Domain.Common.Service;

import com.example.demo.Domain.Common.Dto.ReserveDto;
import com.example.demo.Domain.Common.Entity.FuneralReserve;
import com.example.demo.Domain.Common.Entity.Member;
import com.example.demo.Repository.FuneralReserveRepository;
import com.example.demo.Repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FuneralReserveService {

    private final FuneralReserveRepository reserveRepository;
    private final MemberRepository memberRepository;

    /** 예약 생성 */
    public void createReserve(ReserveDto dto, String username) {
        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("로그인된 회원 정보를 찾을 수 없습니다."));

        FuneralReserve reserve = FuneralReserve.builder()
                .member(member)
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

        reserveRepository.save(reserve);
    }

    /** 회원별 예약 조회 */
    public List<FuneralReserve> getReservesByUsername(String username) {
        return reserveRepository.findByMemberUsername(username);
    }

    /** 예약 조회 (DTO 반환) */
    public ReserveDto getReserveDtoByIdAndUsername(Long id, String username) {
        Optional<FuneralReserve> opt = reserveRepository.findByIdAndMemberUsername(id, username);
        return opt.map(reserve -> {
            ReserveDto dto = new ReserveDto();
            dto.setOwnerName(reserve.getOwnerName());
            dto.setOwnerPhone(reserve.getOwnerPhone());
            dto.setOwnerEmail(reserve.getOwnerEmail());
            dto.setOwnerAddr(reserve.getOwnerAddr());
            dto.setPetName(reserve.getPetName());
            dto.setPetType(reserve.getPetType());
            dto.setPetBreed(reserve.getPetBreed());
            dto.setPetWeight(reserve.getPetWeight());
            dto.setPassedAt(reserve.getPassedAt());
            dto.setPlace(reserve.getPlace());
            dto.setFuneralDate(reserve.getFuneralDate());
            dto.setType(reserve.getType());
            dto.setAsh(reserve.getAsh());
            dto.setPickup(reserve.getPickup());
            dto.setPickupAddr(reserve.getPickupAddr());
            dto.setPickupTime(reserve.getPickupTime());
            dto.setTime(reserve.getTime());
            dto.setMemo(reserve.getMemo());
            return dto;
        }).orElse(null);
    }

    /** 예약 수정 */
    public void updateReserve(Long id, ReserveDto dto, String username) {
        FuneralReserve reserve = reserveRepository.findByIdAndMemberUsername(id, username)
                .orElseThrow(() -> new RuntimeException("예약이 존재하지 않습니다."));

        reserve.setOwnerName(dto.getOwnerName());
        reserve.setOwnerPhone(dto.getOwnerPhone());
        reserve.setOwnerEmail(dto.getOwnerEmail());
        reserve.setOwnerAddr(dto.getOwnerAddr());
        reserve.setPetName(dto.getPetName());
        reserve.setPetType(dto.getPetType());
        reserve.setPetBreed(dto.getPetBreed());
        reserve.setPetWeight(dto.getPetWeight());
        reserve.setPassedAt(dto.getPassedAt());
        reserve.setPlace(dto.getPlace());
        reserve.setFuneralDate(dto.getFuneralDate());
        reserve.setType(dto.getType());
        reserve.setAsh(dto.getAsh());
        reserve.setPickup(dto.getPickup());
        reserve.setPickupAddr(dto.getPickupAddr());
        reserve.setPickupTime(dto.getPickupTime());
        reserve.setTime(dto.getTime());
        reserve.setMemo(dto.getMemo());

        reserveRepository.save(reserve);
    }

    /** 예약 취소 */
    public void deleteReserve(Long id, String username) {
        FuneralReserve reserve = reserveRepository.findByIdAndMemberUsername(id, username)
                .orElseThrow(() -> new RuntimeException("예약이 존재하지 않습니다."));
        reserveRepository.delete(reserve);
    }

}
