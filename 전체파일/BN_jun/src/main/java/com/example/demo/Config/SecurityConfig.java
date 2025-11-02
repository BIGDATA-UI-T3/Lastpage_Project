package com.example.demo.Config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomLoginFailureHandler customLoginFailureHandler;
    private final CustomOAuth2FailureHandler customOAuth2FailureHandler;

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
                                "/", "/signin",
                                "/signup",
                                "/signup/send-email-code",
                                "/css/**", "/asset/**", "/js/**"
                        ).permitAll()

                        .requestMatchers("/mypage", "/reserve/**", "/goods/**").authenticated()
                        .anyRequest().authenticated()
                )
                // 자체 로그인
                .formLogin(form -> form
                        .loginPage("/signin")
                        .loginProcessingUrl("/login-process")
                        .defaultSuccessUrl("/mypage", true)
                        .failureHandler(customLoginFailureHandler)
                        .permitAll()
                )
                // 소셜 로그인
                .oauth2Login(oauth2 -> oauth2
                        .loginPage("/signin")
                        .defaultSuccessUrl("/mypage", true)
                        .failureHandler(customOAuth2FailureHandler)
                )
                // 로그아웃
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/")
                );
        return http.build();
    }
}