package com.example.demo.Domain.Common.Entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OurpageReserve {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 예약자(유저) 시퀀스
    private String userSeq;

    @Column(nullable = false)
    private String petName; // 반려동물 이름

    @Column(nullable = false)
    private String dateRange; // "2010.05.01 ~ 2024.03.15"

    @Column(nullable = false)
    private String message; // 추모 메시지

    private String photoPath; // 업로드된 사진 경로

    @Column(nullable = false)
    private Integer slotIndex;
}