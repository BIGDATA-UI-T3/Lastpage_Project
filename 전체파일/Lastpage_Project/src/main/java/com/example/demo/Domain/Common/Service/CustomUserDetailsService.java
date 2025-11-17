package com.example.demo.Domain.Common.Service;

import com.example.demo.Domain.Common.Entity.Signup;
import com.example.demo.Repository.SignupRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final SignupRepository signupRepository;

    /**
     * username → 자체 로그인(id) 또는 소셜 로그인(userSeq)을 모두 처리
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        log.info("[UserDetails 조회 요청] username={}", username);

        Signup user = null;

        // 1) 자체 로그인 (username = id)
        user = signupRepository.findByIdAndProviderIsNull(username).orElse(null);

        // 2) 소셜 로그인 (username = userSeq)
        if (user == null) {
            user = signupRepository.findById(username).orElse(null);
        }

        if (user == null) {
            throw new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + username);
        }

        log.info("[UserDetails 조회 완료] userSeq={}, id={}, role={}",
                user.getUserSeq(), user.getId(), user.getRole());

        // 기본 권한 설정
        String roleName = (user.getRole() != null) ? user.getRole().name() : "USER";

        return User.withUsername(user.getUserSeq())
                .password(user.getPassword() != null ? user.getPassword() : "{noop}SOCIAL_LOGIN")
                .roles(roleName)
                .build();

    }
}
