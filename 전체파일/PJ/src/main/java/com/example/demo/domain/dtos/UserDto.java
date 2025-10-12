package com.example.demo.domain.dtos;

import lombok.Getter;
import lombok.Setter;

// View(HTML)와 Controller 사이에서 데이터를 담아 옮길 상자(DTO)입니다.
@Getter
@Setter
public class UserDto {
    private String username;
    private String password;
}