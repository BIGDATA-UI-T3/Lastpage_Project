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
                        // [핵심] 이 부분이 없으면 무한 리디렉션 발생
                        .requestMatchers(
                                "/",
                                "/signin", // 로그인 페이지
                                "/signup", // 회원가입 페이지
                                "/css/**", // CSS 폴더 전체
                                "/asset/**", // asset 폴더 전체
                                "/js/**"   // js 폴더 전체 (필요시)
                        ).permitAll()
                        .requestMatchers("/mypage").authenticated()
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/signin") // 로그인 페이지 URL
                        .loginProcessingUrl("/login-process")
                        .defaultSuccessUrl("/mypage", true) // 로그인 성공 시 /mypage로
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/")
                );
        return http.build();
    }
}