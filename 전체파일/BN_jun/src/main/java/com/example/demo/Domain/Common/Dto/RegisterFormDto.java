package com.example.demo.Domain.Common.Dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterFormDto {
    // signup.html의 <input name="...">과 이름이 같아야 합니다.
    private String name;
    private String username;
    private String password;
    private String passwordCheck;
    private String emailId;
    private String emailDomain;
    private String birthYear;
    private String birthMonth;
    private String birthDay;
    private String gender;
    private String phone;

    // (smsCode는 일단 생략)
}