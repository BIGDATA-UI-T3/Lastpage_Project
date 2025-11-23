package com.example.demo.Domain.Common.Dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class WriterDto {
    private String userSeq;
    private String name;
    private String username;  // id 또는 providerId
    private String profileImage;

    @JsonProperty("isFollowing")
    private boolean isFollowing; // 팔로우 여부

    @Builder
    public WriterDto(String userSeq,
                     String name,
                     String username,
                     String profileImage,
                     boolean isFollowing) {
        this.userSeq = userSeq;
        this.name = name;
        this.username = username;
        this.profileImage = profileImage;
        this.isFollowing = isFollowing;
    }

}
