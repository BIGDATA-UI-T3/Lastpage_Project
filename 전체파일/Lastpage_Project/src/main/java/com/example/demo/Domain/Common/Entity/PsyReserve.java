package com.example.demo.Domain.Common.Entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

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

    /** 회원(FK) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_seq", referencedColumnName = "user_seq", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Signup user;


    /** 예약 정보 */
    private String name;
    private String birth;
    @Enumerated(EnumType.STRING)
    private Gender gender;
    private String phone;
    private String email;
    private String address;
    private String consultDate;
    private String counselor;
    private String time;


    @Column(length = 500)
    private String memo;
}
