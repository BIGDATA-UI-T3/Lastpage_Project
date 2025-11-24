package com.example.demo.Domain.Common.Dto;

import com.example.demo.Domain.Common.Entity.Signup;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.nio.channels.AsynchronousChannelGroup;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SignupDto {

    private String userSeq;
    private String id;
    private String name;
    private String password;
    private String confirm_password;
    private String emailId;
    private String emailDomain;
    private Long year;
    private Long month;
    private Long day;
    private String gender;
    private String phone_num;
    private String provider;
    private String providerId;
    private String oauthEmail;
    private String profileImage;
    private String role = "USER";   // 기본 USER

    private LocalDateTime created_at;
    private LocalDateTime updated_at;

    private String bio;

    // follow
    private String username;



    // follow


    /** Signup 엔티티 → SignupDto 변환 */
    public static SignupDto fromEntity(Signup e) {
        SignupDto dto = new SignupDto();
        dto.setUserSeq(e.getUserSeq());
        dto.setId(e.getId());
        dto.setName(e.getName());
        dto.setEmailId(e.getEmailId());
        dto.setEmailDomain(e.getEmailDomain());
        dto.setYear(e.getYear());
        dto.setMonth(e.getMonth());
        dto.setDay(e.getDay());
        dto.setGender(String.valueOf(e.getGender()));
        dto.setPhone_num(e.getPhone_num());
        dto.setProvider(e.getProvider());
        dto.setProviderId(e.getProviderId());
        dto.setOauthEmail(e.getOauthEmail());
        dto.setProfileImage(e.getProfileImage());
        dto.setRole(e.getRole().name());
        dto.setCreated_at(e.getCreated_at());
        dto.setUpdated_at(e.getUpdated_at());
        dto.setUsername(e.getUsername());
        return dto;
    }


}
