package com.example.demo.Domain.Common.Entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name="member")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name; // 이름

    @Column(nullable = false, unique = true)
    private String username; // 아이디

    @Column(nullable = false)
    private String password; // 비밀번호(암호화 필요)

    @Column(nullable = false, unique = true)
    private String email; // 이메일

    private LocalDate birth; // 생년월일

    private String gender; // 남 / 여 / 선택안함

    @Column(unique = true)
    private String phone; // 휴대전화번호

    private boolean phoneVerified; // 인증 완료 여부

    private String provider;
}
