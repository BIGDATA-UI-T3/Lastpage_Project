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
    private String goodsDate;
    private String type;
    private String ash;
    private String pickup;
    private String pickupAddr;
    private String pickupTime;
    private String time;

    @Column(length = 500)
    private String memo;
}
