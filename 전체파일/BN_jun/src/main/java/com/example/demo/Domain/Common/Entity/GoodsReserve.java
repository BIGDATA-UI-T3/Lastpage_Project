//package com.example.demo.Domain.Common.Entity;
//
//import jakarta.persistence.*;
//import lombok.AllArgsConstructor;
//import lombok.Builder;
//import lombok.Data;
//import lombok.NoArgsConstructor;
//
//@Entity
//@Table(name = "goods_reserve")
//@Data
//@NoArgsConstructor
//@AllArgsConstructor
//@Builder
//public class GoodsReserve {
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//    private String ownerName;
//    private String ownerPhone;
//    private String ownerEmail;
//    private String ownerAddr;
//    private String petName;
//    private String petType;
//    private String petBreed;
//    private String petWeight;
//
//    private String passedAt;
//    private String place;
//    private String goodsDate;
//    private String type;
//    private String ash;
//    private String pickup;
//    private String pickupAddr;
//    private String pickupTime;
//    private String time;
//
//    @Column(length = 500)
//    private String memo;
//}

package com.example.demo.Domain.Common.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "goods_reserve")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GoodsReserve {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // === 기존 필드 ===
    private String ownerName;
    private String ownerPhone;
    private String ownerEmail;
    private String ownerAddr;
    private String petName;
    private String petType;
    private String petBreed;
    private String petWeight;
    @Column(length = 500)
    private String memo;

    // === Step 1 추가 ===
    private String materials; // DB에는 "ash,hair" 처럼 문자열로 저장

    // === Step 2 추가 ===
    private String product;
    private String metalColor;
    private String chainLength;
    private String ringSize;
    private Integer quantity;
    private String engravingText;
    private String engravingFont;
    @Column(length = 500)
    private String optionsMemo;

    // === Step 3 추가 ===
    private String shipMethod;
    private String targetDate;
    private Boolean isExpress;
    private String kitAddr;
    private String kitDate;
    private String kitTime;
    private String visitDate;
    private String visitTime;
    private String trackingInfo;
}
