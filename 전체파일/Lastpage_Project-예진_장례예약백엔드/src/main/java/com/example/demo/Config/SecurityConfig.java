package com.example.demo.Config;

import com.example.demo.Domain.Common.Service.CustomOAuth2UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;


@RequiredArgsConstructor
@Configuration
public class SecurityConfig {

    private final CustomOAuth2UserService customOAuth2UserService;
    private final ClientRegistrationRepository clientRegistrationRepository;
    private final String KAKAO_CLIENT_ID = "";


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
                        .requestMatchers("/mypage/**").authenticated()
                        .anyRequest().permitAll()
                )
                // formLogin 비활성화
                .formLogin(form -> form.disable()
                )
                // logot 비활성화
                .logout(logout -> logout
                                .logoutUrl("/logout")
                                .logoutSuccessHandler((request, response, authentication) -> {
                                    // Spring 세션 종료
                                    if (authentication != null) {
                                        request.getSession().invalidate();
                                        System.out.println("로그아웃: Spring 세션 종료");
                                    }

                                    // 카카오 로그아웃 URL (client_id 중복 제거!)
                                    String kakaoLogoutUrl = "https://kauth.kakao.com/oauth/logout?client_id="
                                            + KAKAO_CLIENT_ID
                                            + "&logout_redirect_uri=http://localhost:8080/";

                                    // 브라우저에서 카카오 로그아웃 호출
                                    response.sendRedirect(kakaoLogoutUrl);
                                })
                                .permitAll()
                )
                .sessionManagement(session -> session
                        .sessionFixation().migrateSession() // 세션 고정 보호
                        .maximumSessions(1)                 // 동시에 1명만 로그인
                        .maxSessionsPreventsLogin(false)
                        .expiredUrl("/signin?expired=true") // 세션 만료시 이동할 페이지
                )

                // 카카오로그인
                .oauth2Login(oauth2 -> oauth2
                        .loginPage("/signin")
                        // ClientRegistrationRepository 주입된 resolver 사용
//                        .authorizationEndpoint(authEndpoint -> authEndpoint
//                                .authorizationRequestResolver(
//                                        new CustomAuthorizationRequestResolver(
//                                                clientRegistrationRepository,
//                                                "/oauth2/authorization"
//                                        )
//                                )
//                        )
                        .defaultSuccessUrl("/", true)
                        .userInfoEndpoint(user -> user.userService(customOAuth2UserService)) //
        );
        return http.build();
    }

//    // 로그아웃 성공 시 터미널에 출력
//    @Bean
//    public LogoutSuccessHandler logoutSuccessHandler() {
//        return (HttpServletRequest request, HttpServletResponse response, org.springframework.security.core.Authentication authentication) -> {
//            System.out.println("로그아웃");
//            response.sendRedirect("/"); // 로그아웃 후 메인페이지 이동
//        };
//    }


}
