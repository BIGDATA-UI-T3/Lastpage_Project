package com.example.demo.Domain.Common.Dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class FindPasswordResponseDto {
    private String result;
    private String message;

}
