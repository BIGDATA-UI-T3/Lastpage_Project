package com.example.demo.Domain.Common.Service;

import com.example.demo.Domain.Common.Dto.SignupDto;
import com.example.demo.Domain.Common.Entity.PsyReserve;
import com.example.demo.Domain.Common.Entity.Signup;
import com.example.demo.Repository.SignupRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class SignupService {
    private final SignupRepository repository;
    public Signup saveUserInfo(SignupDto dto){
        Signup entity = Signup.builder()
                .name(dto.getName())
                .id(dto.getId())
                .password(dto.getPassword())
                .confirm_password(dto.getConfirm_password())
                .email_id(dto.getEmail_id())
                .email_domain(dto.getEmail_domain())
                .year(dto.getYear())
                .month(dto.getMonth())
                .day(dto.getDay())
                .gender(dto.getGender())
                .phone_num(dto.getPhone_num())
                .sms_auth_number(dto.getSms_auth_number())
                .created_at(dto.getCreated_at())
                .updated_at(dto.getUpdated_at())
                .build();

        return repository.save(entity);
    }


}
