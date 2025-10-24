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
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(
                                "/", "/signin", "/signup", "/css/**", "/asset/**", "/js/**"
                        ).permitAll()
                        .requestMatchers("/mypage").authenticated()
                        .anyRequest().authenticated()
                )
                // [1. 자체 로그인]
                .formLogin(form -> form
                        .loginPage("/signin")
                        .loginProcessingUrl("/login-process")
                        .defaultSuccessUrl("/mypage", true)
                        .permitAll()
                )

                .oauth2Login(oauth2 -> oauth2
                        .loginPage("/signin") // 👈 소셜 로그인을 눌러도 우리 로그인 페이지(/signin)에서 시작
                        .defaultSuccessUrl("/mypage", true) // 👈 소셜 로그인 성공 시 /mypage로 이동
                )

                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/")
                );
        return http.build();
    }
}