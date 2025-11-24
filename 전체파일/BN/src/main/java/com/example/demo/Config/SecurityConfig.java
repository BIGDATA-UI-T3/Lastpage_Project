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
@EnableMethodSecurity(prePostEnabled = true)   // ★ 반드시 추가!
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

        http.authorizeHttpRequests(auth -> auth
                .requestMatchers("/css/**", "/js/**", "/Asset/**").permitAll()
                .requestMatchers("/signin", "/loginProc","/api/loginProc" ,"/signup", "/login/**", "/login/oauth2/**").permitAll()
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .anyRequest().permitAll()


        );

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
