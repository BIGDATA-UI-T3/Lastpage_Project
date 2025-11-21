package com.example.demo.Config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                //  CSRF 비활성화 (테스트 중이라면; 실제 배포시엔 활성화 고려)
                .csrf(csrf -> csrf.disable())

                //  URL 접근 권한 설정
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/signin",        // 로그인 페이지
                                "/signup",        // 회원가입 페이지
                                "/loginProc",     // 로그인 처리 URL
                                "/oauth2/**",     // 소셜 로그인 콜백
                                "/css/**", "/js/**", "/images/**", "/Asset/**", "/api/community/like/**"
                        ).permitAll()
                        .anyRequest().permitAll() // 나머지도 임시로 모두 허용
                )

                //  기본 폼 로그인 완전히 비활성화
                .formLogin(form -> form.disable())

                //  기본 로그아웃 기능도 비활성화 (직접 구현할 예정)
                .logout(logout -> logout.disable());

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        // 비밀번호 암호화용 BCrypt 인코더 등록
        return new BCryptPasswordEncoder();
    }
}