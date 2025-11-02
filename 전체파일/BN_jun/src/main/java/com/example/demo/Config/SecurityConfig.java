package com.example.demo.Config;

import lombok.RequiredArgsConstructor; // [추가]
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor // [추가]
public class SecurityConfig {

    // [추가] 2. 새로 생성한 핸들러를 주입받습니다.
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

                        // [수정] /reserve 경로도 인증이 필요하도록 추가합니다.
                        .requestMatchers("/mypage", "/reserve/**", "/goods/**").authenticated()
                        .anyRequest().authenticated()
                )
                // 자체 로그인
                .formLogin(form -> form
                        .loginPage("/signin")
                        .loginProcessingUrl("/login-process")
                        .defaultSuccessUrl("/mypage", true)
                        // [수정] 3. 자체 로그인 실패 핸들러 등록
                        .failureHandler(customLoginFailureHandler)
                        .permitAll()
                )
                // 소셜 로그인
                .oauth2Login(oauth2 -> oauth2
                        .loginPage("/signin")
                        .defaultSuccessUrl("/mypage", true)
                        // [수정] 4. 소셜 로그인 실패 핸들러 등록
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