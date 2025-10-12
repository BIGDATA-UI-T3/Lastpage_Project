package com.example.demo.service;

import com.example.demo.domain.dtos.UserDto;
import com.example.demo.domain.entity.User;
import com.example.demo.domain.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.security.crypto.password.PasswordEncoder; // [수정] 삭제
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    // @Autowired
    // private PasswordEncoder passwordEncoder; // [수정] 삭제

    public void registerUser(UserDto userDto) {
        User user = new User();
        user.setUsername(userDto.getUsername());

        // [수정] 암호화 없이 받은 비밀번호를 그대로 저장합니다.
        user.setPassword(userDto.getPassword());

        userRepository.save(user);
    }
}