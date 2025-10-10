package com.example.demo.Domain.Common.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "psy_reserve")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PsyReserve {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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

    @Column(length = 500)
    private String memo;
}
