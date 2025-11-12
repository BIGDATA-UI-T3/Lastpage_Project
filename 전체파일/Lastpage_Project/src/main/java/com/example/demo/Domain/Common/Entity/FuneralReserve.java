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

    @Column(length = 500)
    private String memo;


}
