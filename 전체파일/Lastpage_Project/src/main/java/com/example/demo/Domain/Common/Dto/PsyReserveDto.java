package com.example.demo.Domain.Common.Dto;

import com.example.demo.Domain.Common.Entity.Gender;
import com.example.demo.Domain.Common.Entity.PsyReserve;
import com.example.demo.Domain.Common.Entity.Signup;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PsyReserveDto {

    private Long id;

    /** FK로 연결된 회원 UUID */
    private String userSeq;

    /** 예약 관련 정보 */
    private String name;
    private String birth;
    private Gender gender;
    private String phone;
    private String email;
    private String address;
    private String consultDate;
    private String time;
    private String counselor;
    private String memo;

    /** Entity → DTO 변환 */
    public static PsyReserveDto fromEntity(PsyReserve entity) {
        if (entity == null) return null;

        return PsyReserveDto.builder()
                .id(entity.getId())
                .userSeq(entity.getUser().getUserSeq())
                .name(entity.getName())
                .birth(entity.getBirth())
                .gender(entity.getGender())
                .phone(entity.getPhone())
                .email(entity.getEmail())
                .address(entity.getAddress())
                .consultDate(entity.getConsultDate())
                .time(entity.getTime())
                .counselor(entity.getCounselor())
                .memo(entity.getMemo())
                .build();
    }

    /** DTO → Entity 변환 */
    public PsyReserve toEntity(Signup user) {
        return PsyReserve.builder()
                .id(this.id)
                .user(user)  // FK 매핑
                .name(this.name)
                .birth(this.birth)
                .gender(this.gender)
                .phone(this.phone)
                .email(this.email)
                .address(this.address)
                .consultDate(this.consultDate)
                .time(this.time)
                .counselor(this.counselor)
                .memo(this.memo)
                .build();
    }


}
