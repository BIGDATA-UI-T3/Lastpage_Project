package com.example.demo.Domain.Common.Dto;

import lombok.Data;
import java.util.List; // List 타입을 위해 추가

@Data
public class GoodsReserveDto {
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
    private List<String> materials;

    // === Step 2 추가 ===
    private String product;
    private String metalColor;
    private String chainLength;
    private String ringSize;
    private Integer quantity;
    private String engravingText;
    private String engravingFont;
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