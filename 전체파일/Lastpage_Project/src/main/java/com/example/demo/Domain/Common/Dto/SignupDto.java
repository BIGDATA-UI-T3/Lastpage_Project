package com.example.demo.Domain.Common.Dto;

import com.example.demo.Domain.Common.Entity.Gender;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SignupDto {

    /**  일반 회원가입 공통 정보 */
    private String name;
    private String id;  // 일반 회원의 로그인 ID
    private String password;
    private String confirm_password;
    private String emailId;
    private String emailDomain;
    private Long year;
    private Long month;
    private Long day;
    private Gender gender;
    private String phone_num;
    private String sms_auth_number;
    private LocalDateTime created_at;
    private LocalDateTime updated_at;

    //  소셜 로그인용 필드
    private String provider;
    private String providerId;
    private String oauthEmail;
    private String profileImage;
}
