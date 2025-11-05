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
    @JoinColumn(name = "userSeq", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE) // 조인할 때 만약 회원에서 키값이 날라가면 당연하게 외래키를 사용하는 예약 테이블도 사라져야 함.
    private Signup user;   // FK로 Signup의 UUID 참조

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
