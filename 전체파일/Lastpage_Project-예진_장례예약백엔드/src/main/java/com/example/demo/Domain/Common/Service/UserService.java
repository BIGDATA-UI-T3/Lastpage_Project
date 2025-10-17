package com.example.demo.Domain.Common.Service;

import com.example.demo.Domain.Common.Dto.UserDto;
import com.example.demo.Domain.Common.Entity.User;
import com.example.demo.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    // [추가] 이 메서드가 하나의 작업 단위(트랜잭션)임을 선언합니다.
    @Transactional
    public void registerUser(UserDto userDto) {
        User user = new User();
        user.setUsername(userDto.getUsername());
        user.setPassword(userDto.getPassword());

        userRepository.save(user);
    }
}