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
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // [수정] CSRF 비활성화 (이전 JS 코드와 맞추기 위해)
                .csrf(csrf -> csrf.disable())

                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(
                                "/", "/signin",
                                "/signup",
                                "/signup/send-email-code",
                                "/css/**", "/asset/**", "/js/**"
                        ).permitAll()

                        .requestMatchers("/mypage").authenticated() // 로그인한 사람만
                        .anyRequest().authenticated() // 그 외는 로그인 필수
                )
                // 자체 로그인
                .formLogin(form -> form
                        .loginPage("/signin")
                        .loginProcessingUrl("/login-process")
                        .defaultSuccessUrl("/mypage", true)
                        .permitAll()
                )
                // 소셜 로그인
                .oauth2Login(oauth2 -> oauth2
                        .loginPage("/signin")
                        .defaultSuccessUrl("/mypage", true)
                )
                // 로그아웃
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/")
                );
        return http.build();
    }
}