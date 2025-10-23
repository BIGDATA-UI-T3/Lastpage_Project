package com.example.demo.Domain.Common.Entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDateTime;

@Entity
@Table(name="user_info")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Signup {
    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(length = 36)
    private String user_seq; // DB 기본키
    private String name;


    private String id;
    private String password;
    private String confirm_password;
    private String email_id;
    private String email_domain;
    private Long year;
    private Long month;
    private Long day;

    @Enumerated(EnumType.STRING)
    private Gender gender;
    private String phone_num;
    private String sms_auth_number;
    private LocalDateTime created_at;
    private LocalDateTime updated_at;

}
