package com.example.demo.Domain.Common.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

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
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_seq", referencedColumnName = "user_seq", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Signup user;


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
    private String materials;

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
    private Boolean consent; // 동의 여부
}
