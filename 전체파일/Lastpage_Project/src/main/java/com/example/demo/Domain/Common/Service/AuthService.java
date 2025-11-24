package com.example.demo.Domain.Common.Service;

import com.example.demo.Domain.Common.Entity.Role;
import com.example.demo.Domain.Common.Entity.Signup;
import com.example.demo.Repository.SignupRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final SignupRepository repository;
    private final PasswordEncoder passwordEncoder;

    /**
     * 자체 로그인 전용 인증 로직
     *  - provider == null 인 회원만 로그인 허용
     *  - 비밀번호 비교
     *  - ROLE 값 확인
     */
    public Signup authenticate(String id, String password) {

        // 1) 자체 로그인 사용자 찾기 (provider=null)
        Signup user = repository.findByIdAndProviderIsNull(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 아이디입니다."));

        // 2) 비밀번호 검증
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        // 3) Role null 보호 — 기본 USER 설정
        if (user.getRole() == null) {
            log.warn("[경고] Role 값이 null → ROLE_USER로 초기화");
            user.setRole(Role.USER);
        }

        log.info("[AuthService 인증 성공] id={}, userSeq={}, role={}",
                user.getId(), user.getUserSeq(), user.getRole().name());

        return user;
    }
}
