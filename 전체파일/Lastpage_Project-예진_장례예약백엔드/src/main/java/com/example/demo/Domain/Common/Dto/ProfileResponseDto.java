package com.example.demo.Domain.Common.Dto;

import lombok.Data;

@Data
public class ProfileResponseDto {
    private boolean isLogin;
    private String name;      // 이름
    private String username;  // 아이디
    private String bio;       // 소개글
    private String avatar;    // 프로필 이미지 경로 (없으면 null)
}
