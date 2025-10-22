package com.example.demo.Domain.Common.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "user")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username; // 로그인 아이디

    private String password; // 비밀번호 (암호화되어 저장됨)

    private String name;     // 이름 (signup.html의 '이름')

    private String email;    // 이메일 (signup.html의 '이메일')

    private String phone;    // 휴대폰 (signup.html의 '휴대폰')

    private String role;     // 권한 (예: "ROLE_USER", "ROLE_ADMIN")

    // (생년월일, 성별 등은 DTO에서 처리하거나 필요시 Entity에 추가합니다)
}