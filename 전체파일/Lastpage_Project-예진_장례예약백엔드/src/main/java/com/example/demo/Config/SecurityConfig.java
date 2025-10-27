package com.example.demo.Config;

import com.example.demo.Domain.Common.Service.CustomOAuth2UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.SecurityFilterChain;

@RequiredArgsConstructor
@Configuration
public class SecurityConfig {

    private final CustomOAuth2UserService customOAuth2UserService; //

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // 테스트용 비활성화
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/", "/signin", "/signup", "/member/**",
                                "/css/**", "/js/**", "/images/**"
                        ).permitAll()
                        .anyRequest().permitAll()
                )
                // formLogin 비활성화
                .formLogin(form -> form.disable())
                // logot 비활성화
                .logout(logout -> logout.disable())
                // 카카오로그인
                .oauth2Login(oauth2 -> oauth2
                .loginPage("/signin")
                .defaultSuccessUrl("/", true)
                .userInfoEndpoint(user -> user.userService(customOAuth2UserService)) //
        );
        return http.build();
    }


}
