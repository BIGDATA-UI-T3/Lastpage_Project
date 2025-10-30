package com.example.demo.Domain.Common.Dto;

import lombok.Data;

@Data
public class PsyReserveDto {
    private Long id;
    private String name;
    private String birth;
    private String gender;
    private String phone;
    private String email;
    private String address;
    private String consultDate;
    private String time;
    private String counselor;
    private String memo;
}
