package com.example.demo.Domain.Common.Dto;


import com.example.demo.Domain.Common.Entity.Gender;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SignupDto {

    private String name;
    private String id;
    private String password;
    private String confirm_password;
    private String email_id;
    private String email_domain;
    private Long year;
    private Long month;
    private Long day;


    private Gender gender;
    private String phone_num;
    private String sms_auth_number;
    private LocalDateTime created_at;
    private LocalDateTime updated_at;

}
