package com.example.demo.Config;

import com.example.demo.Domain.Common.Service.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)   //  반드시 추가!
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomUserDetailsService customUserDetailsService;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http.csrf(csrf -> csrf.disable());

        // ★ SecurityContext 를 세션에 저장하도록 강제
        http.securityContext(security -> security
                .securityContextRepository(
                        new org.springframework.security.web.context.HttpSessionSecurityContextRepository()
                )
        );

        http.userDetailsService(customUserDetailsService);

        // =========================================================================
        // 챗봇 API 및 문서 경로 추가 (JSON 오류 방지)
        // =========================================================================
        http.authorizeHttpRequests(auth -> auth

                // 기존 정적 리소스 및 로그인 경로
                .requestMatchers("/css/**", "/js/**", "/Asset/**").permitAll()
                .requestMatchers("/signin", "/loginProc","/api/loginProc" ,"/signup", "/login/**", "/login/oauth2/**").permitAll()

                // -----------------------------------------------------------------
                // ★ 챗봇/OpenAPI 경로 추가: JSON 응답이 HTML로 바뀌는 것을 방지
                // -----------------------------------------------------------------
                .requestMatchers(
                        "/api/v1/chat",          // 고급 챗봇
                        "/api/v1/simple-chat",   // 단순 챗봇
                        "/v3/api-docs",          // OpenAPI 문서 JSON
                        "/swagger-ui/**"         // Swagger UI 리소스
                ).permitAll()

                .requestMatchers("/admin/**").hasRole("ADMIN")
                .anyRequest().permitAll()

        );
        // =========================================================================


        http.formLogin(form -> form.disable());

        http.exceptionHandling()
                .authenticationEntryPoint(
                        (req, res, ex) -> res.sendRedirect("/signin")
                );

        http.logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/")
        );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}