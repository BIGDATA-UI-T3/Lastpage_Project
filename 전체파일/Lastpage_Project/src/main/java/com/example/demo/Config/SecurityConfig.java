package com.example.demo.Config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;



@Configuration
public class SecurityConfig {

//    @Bean
//    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
//        http
//                .csrf().disable() // CSRF 비활성화 (테스트용)
//                .authorizeHttpRequests(auth -> auth
//                        .requestMatchers("/signin", "/signup", "/loginProc", "/css/**", "/js/**", "/images/**").permitAll() // 로그인/회원가입 페이지 접근 허용
//                        .anyRequest().permitAll() // 나머지도 임시로 모두 허용
//                )
//                .formLogin().disable() // ✅ 기본 로그인 폼 비활성화
//                .logout().disable();   // ✅ 기본 로그아웃 비활성화
//
//        return http.build();
//    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
