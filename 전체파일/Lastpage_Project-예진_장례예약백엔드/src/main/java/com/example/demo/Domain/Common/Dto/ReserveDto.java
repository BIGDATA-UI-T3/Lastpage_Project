//package com.example.demo.Domain.Common.Dto;
//
//import lombok.Data;
//
//@Data
//public class ReserveDto{
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
//    private String memo;
//
//}

package com.example.demo.Domain.Common.Dto;

import lombok.Data;
import java.util.List; // List 타입을 위해 추가

@Data
public class ReserveDto{
    // === 기존 필드 ===
    private String ownerName;
    private String ownerPhone;
    private String ownerEmail;
    private String ownerAddr;
    private String petName;
    private String petType;
    private String petBreed;
    private String petWeight;
    private String memo;

    // === Step 1 추가 ===
    private List<String> materials; // JS에서 배열로 오므로 List로 받는 것이 편리

    // === Step 2 추가 ===
    private String product;
    private String metalColor; // 'metal' -> 'metalColor'로 변경 추천
    private String chainLength;
    private String ringSize;
    private Integer quantity;   // 'qty' -> 'quantity'로 변경 추천, 숫자는 Integer로
    private String engravingText; // 'engrave' -> 'engravingText'로 변경 추천
    private String engravingFont; // 'font' -> 'engravingFont'로 변경 추천
    private String optionsMemo;   // 'optMemo' -> 'optionsMemo'로 변경 추천

    // === Step 3 추가 ===
    private String shipMethod;
    private String targetDate;
    private Boolean isExpress;    // 'express' -> 'isExpress'로 변경 추천, 참/거짓은 Boolean으로
    private String kitAddr;
    private String kitDate;
    private String kitTime;       // 'time' -> 'kitTime'으로 변경 추천 (수거 시간)
    private String visitDate;
    private String visitTime;
    private String trackingInfo;  // 'tracking' -> 'trackingInfo'로 변경 추천
}