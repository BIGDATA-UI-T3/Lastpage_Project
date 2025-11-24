package com.example.demo.Domain.Common.Entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor @AllArgsConstructor
public class Follow {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String userUsername;     // 팔로우 하는 사람 username
    private String targetUsername;   // 팔로우 받는 사람 username

    public Follow(String userUsername, String targetUsername) {
        this.userUsername = userUsername;
        this.targetUsername = targetUsername;
    }
}
