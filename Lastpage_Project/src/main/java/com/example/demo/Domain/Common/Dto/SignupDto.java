package com.example.demo.Domain.Common.Dto;

import com.example.demo.Domain.Common.Entity.Gender;
import com.example.demo.Domain.Common.Entity.Signup;
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

    /**
     * 내부 고유 식별자 (UUID)
     */
    private String userSeq;

    /**
     * 일반 회원가입 공통 정보
     */
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

    // 소셜 로그인용 필드
    private String provider;
    private String providerId;
    private String oauthEmail;

    // 커뮤니티 프로필
    private String profileImage;
    private String bio;

    // follow
    private String username;

    public static SignupDto fromEntity(Signup entity) {
        if (entity == null) return null;

        return SignupDto.builder()
                .userSeq(entity.getUserSeq())
                .name(entity.getName())
                .id(entity.getId())
                .emailId(entity.getEmailId())
                .emailDomain(entity.getEmailDomain())
                .year(entity.getYear())
                .month(entity.getMonth())
                .day(entity.getDay())
                .gender(entity.getGender())
                .phone_num(entity.getPhone_num())
                .sms_auth_number(entity.getSms_auth_number())
                .created_at(entity.getCreated_at())
                .updated_at(entity.getUpdated_at())
                .provider(entity.getProvider())
                .providerId(entity.getProviderId())
                .oauthEmail(entity.getOauthEmail())
                .profileImage(entity.getProfileImage())
                .bio(entity.getBio())
                .build();
    }
}


