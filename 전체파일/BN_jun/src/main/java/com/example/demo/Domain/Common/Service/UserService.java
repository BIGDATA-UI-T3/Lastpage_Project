package com.example.demo.Domain.Common.Service;

import com.example.demo.Domain.Common.Dto.RegisterFormDto;
import com.example.demo.Domain.Common.Entity.User;
import com.example.demo.Repository.UserRepository;
import lombok.AllArgsConstructor;

// ▼▼▼ [추가] 권한(Role) 관련 4개 import
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List; // [추가]

@Service
@AllArgsConstructor
public class UserService implements UserDetailsService { // (implements UserDetailsService 확인)

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // [기능 1] 회원가입 로직 (수정 없음, 완벽함)
    @Transactional
    public User registerUser(RegisterFormDto dto) {
        if (userRepository.findByUsername(dto.getUsername()).isPresent()) {
            throw new IllegalStateException("이미 존재하는 아이디입니다.");
        }
        if (!dto.getPassword().equals(dto.getPasswordCheck())) {
            throw new IllegalStateException("비밀번호가 일치하지 않습니다.");
        }

        User user = new User();
        user.setName(dto.getName());
        user.setUsername(dto.getUsername());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setEmail(dto.getEmailId() + "@" + dto.getEmailDomain());
        user.setPhone(dto.getPhone());
        user.setRole("ROLE_USER"); // "ROLE_" 접두사가 붙는 것이 규칙입니다.

        return userRepository.save(user);
    }

    // ▼▼▼ [기능 2] 로그인 로직 (이 부분을 수정해야 합니다!) ▼▼▼
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // DB에서 아이디로 사용자를 찾음
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("아이디를 찾을 수 없습니다: " + username));

        // [수정] 사용자의 'role'을 기반으로 실제 권한(Authority) 목록 생성
        List<GrantedAuthority> authorities = new ArrayList<>();
        if (user.getRole() != null) {
            authorities.add(new SimpleGrantedAuthority(user.getRole()));
        }

        // [수정] Security가 이해하는 User 객체에 '권한 목록'을 담아서 반환
        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                authorities // 👈 빈 리스트 대신, 실제 권한(authorities)을 전달
        );
    }
}