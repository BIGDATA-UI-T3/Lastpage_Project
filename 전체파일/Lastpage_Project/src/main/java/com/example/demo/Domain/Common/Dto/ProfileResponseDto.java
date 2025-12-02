package com.example.demo.Domain.Common.Dto;

import com.example.demo.Domain.Common.Entity.Signup;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProfileResponseDto {
    private String profileImage;
    private String bio;
    private boolean login;
    private String name;
    private String username;

    private int followerCount;
    private int followingCount;

    public ProfileResponseDto(Signup user) {
        this.name = user.getName();
        this.bio = user.getBio();
        this.profileImage = user.getProfileImage();
        this.username = user.getUsername();
        this.login = true;
    }
}
