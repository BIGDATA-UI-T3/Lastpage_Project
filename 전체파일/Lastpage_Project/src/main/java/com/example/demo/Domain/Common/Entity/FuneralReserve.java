package com.example.demo.Domain.Common.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(name = "funeral_reserve")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FuneralReserve {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_seq", referencedColumnName = "user_seq", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Signup user;

    @Column(name = "owner_name")
    private String ownerName;
    @Column(name = "owner_phone")
    private String ownerPhone;
    @Column(name = "owner_email")
    private String ownerEmail;
    @Column(name = "owner_addr")
    private String ownerAddr;
    @Column(name = "pet_name")
    private String petName;
    @Column(name = "pet_type")
    private String petType;
    @Column(name = "pet_breed")
    private String petBreed;
    @Column(name = "pet_weight")
    private String petWeight;
    @Column(name = "passed_at")
    private String passedAt;
    private String place;
    @Column(name = "funeral_date")
    private String funeralDate;
    private String type;
    private String ash;
    private String pickup;
    @Column(name = "pickup_addr")
    private String pickupAddr;
    @Column(name = "pickup_time")
    private String pickupTime;
    private String time;

    @Column(length = 500)
    private String memo;


}
