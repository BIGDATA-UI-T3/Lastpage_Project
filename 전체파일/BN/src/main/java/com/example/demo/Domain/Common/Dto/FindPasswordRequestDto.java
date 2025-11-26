package com.example.demo.Domain.Common.Dto;

import jakarta.persistence.Column;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FindPasswordRequestDto {
    private String id;
    @Column(name = "email_id")
    private String emailId;

    @Column(name = "email_domain")
    private String emailDomain;


}
