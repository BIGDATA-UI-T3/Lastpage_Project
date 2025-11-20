package com.example.demo.Domain.Common.Dto;

import lombok.*;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OurpageReserveDto {

    private Long id;            // 예약 PK

    private String petName;

    private String dateStart;
    private String dateEnd;

    private String dateRange;   // "2025.01.01 ~ 2025.01.03"

    private String message;     // 메시지

    private MultipartFile petPhoto;  // 업로드 파일
    private String petPhotoPath;     // 저장 파일 경로

    private Integer slotIndex;  // Ourpage 슬롯 위치 (0~8)

    private String userSeq;     // 유저 SEQ

    private boolean occupied;   // 해당 슬롯에 데이터 있음?
    private String photoUrl;    // 사진 URL
}
