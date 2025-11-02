package com.example.demo.Domain.Common.Entity;

import com.fasterxml.jackson.annotation.JsonIgnore; // [추가] 1. JsonIgnore import
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

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
    private String username;
    private String password;
    private String name;

    @Column(unique = true)
    private String email;

    private String phone;
    private String role;

    @JsonIgnore
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<GoodsReserve> goodsReserves = new ArrayList<>();
}