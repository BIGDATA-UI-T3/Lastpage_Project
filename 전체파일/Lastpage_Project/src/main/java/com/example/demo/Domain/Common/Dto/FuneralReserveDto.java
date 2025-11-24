package com.example.demo.Domain.Common.Dto;

import com.example.demo.Domain.Common.Entity.FuneralReserve;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FuneralReserveDto {
    private Long id;

    private String userSeq;
    private String ownerName;
    private String ownerPhone;
    private String ownerEmail;
    private String ownerAddr;
    private String petName;
    private String petType;
    private String petBreed;
    private String petWeight;

    private String passedAt;
    private String place;
    private String funeralDate;
    private String type;
    private String ash;
    private String pickup;
    private String pickupAddr;
    private String pickupTime;
    private String time;

    private String memo;

    private String username;

    public static FuneralReserveDto fromEntity(FuneralReserve f) {
        FuneralReserveDto dto = new FuneralReserveDto();

        dto.setId(f.getId());
        dto.setUserSeq(f.getUser().getUserSeq());

        dto.setOwnerName(f.getOwnerName());
        dto.setOwnerPhone(f.getOwnerPhone());
        dto.setOwnerEmail(f.getOwnerEmail());
        dto.setOwnerAddr(f.getOwnerAddr());

        dto.setPetName(f.getPetName());
        dto.setPetType(f.getPetType());
        dto.setPetBreed(f.getPetBreed());
        dto.setPetWeight(f.getPetWeight());

        dto.setPassedAt(f.getPassedAt());
        dto.setPlace(f.getPlace());
        dto.setFuneralDate(f.getFuneralDate());
        dto.setType(f.getType());
        dto.setAsh(f.getAsh());
        dto.setPickup(f.getPickup());
        dto.setPickupAddr(f.getPickupAddr());
        dto.setPickupTime(f.getPickupTime());
        dto.setTime(f.getTime());
        dto.setMemo(f.getMemo());

        // userName은 Signup에서 가져올 수 있음
        dto.setUsername(f.getUser().getName());

        return dto;
    }


//    public Long getId() { return id; }
//    public void setId(Long id) { this.id = id; }
}
