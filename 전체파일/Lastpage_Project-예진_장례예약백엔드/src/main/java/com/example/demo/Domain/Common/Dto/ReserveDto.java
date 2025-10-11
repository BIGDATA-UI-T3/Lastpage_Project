package com.example.demo.Domain.Common.Dto;

import lombok.Data;

@Data
public class ReserveDto{
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

    private String memo;

}
