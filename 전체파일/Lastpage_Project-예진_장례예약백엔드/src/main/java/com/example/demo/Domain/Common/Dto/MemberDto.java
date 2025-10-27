package com.example.demo.Domain.Common.Dto;

import com.example.demo.Domain.Common.Entity.Member;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.time.LocalDate;

@Data
@NoArgsConstructor
public class MemberDto {
    private String name;
    private String username;
    private String password;
    private String confirmPassword;
    private String email;
//    @DateTimeFormat(pattern="yyyy-MM-dd")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate birth;
    private String gender;
    private String phone;
    private String authCode; // 인증번호 확인용

    // Member 엔티티를 받아 DTO로 변환하는 생성자
    public MemberDto(Member member) {
        this.name = member.getName();
        this.username = member.getUsername();
        this.email = member.getEmail();
        this.birth = member.getBirth();
        this.gender = member.getGender();
        this.phone = member.getPhone();
    }
}




