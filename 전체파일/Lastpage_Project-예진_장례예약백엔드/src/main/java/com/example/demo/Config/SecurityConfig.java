package com.example.demo.Config;

import com.example.demo.Domain.Common.Service.CustomOAuth2UserService;
import com.example.demo.Handler.OAuth2LoginSuccessHandler;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.web.client.RestTemplate;

import java.util.Map;


@RequiredArgsConstructor
@Configuration
public class SecurityConfig {

    private final CustomOAuth2UserService customOAuth2UserService;
    private final ClientRegistrationRepository clientRegistrationRepository;

    private final String KAKAO_CLIENT_ID = "";
    private static final String NAVER_CLIENT_ID = "";
    private static final String NAVER_CLIENT_SECRET = "";


    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // 테스트용 비활성화
                // 접근권한
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/", "/signin", "/signup", "/member/**",
                                "/css/**", "/js/**", "/images/**"
                        ).permitAll()
                        .requestMatchers("/mypage/**","/reserve/**").authenticated()
                        .anyRequest().permitAll()
                )
                // formLogin 비활성화
                .formLogin(form -> form.disable()
                )
                // logot
//                .logout(logout -> logout
//                                .logoutUrl("/logout")
//                                .logoutSuccessHandler((request, response, authentication) -> {
//                                    // Spring 세션 종료
//                                    if (authentication != null) {
//                                        request.getSession().invalidate();
//                                        System.out.println("로그아웃: Spring 세션 종료");
//                                    }
//
//                                    // 현재 로그인한 provider 판별
//                                    Object principal = (authentication != null) ? authentication.getPrincipal() : null;
//                                    if (principal instanceof OAuth2User oAuth2User) {
//                                        Object provider = oAuth2User.getAttributes().get("provider");
//
//                                        if ("KAKAO".equals(provider)) {
//                                            // 카카오 로그아웃
//                                            String kakaoLogoutUrl = "https://kauth.kakao.com/oauth/logout?client_id="
//                                                    + KAKAO_CLIENT_ID
//                                                    + "&logout_redirect_uri=http://localhost:8080/";
//                                            response.sendRedirect(kakaoLogoutUrl);
//                                            return;
//                                        } else if ("NAVER".equals(provider)) {
//                                            // 네이버 로그아웃 (토큰 만료용)
//                                            response.sendRedirect("https://nid.naver.com/nidlogin.logout");
//                                            return;
//                                        }
//                                    }
//
//                                    // 기본 리다이렉트 (로그인 페이지 or 홈)
//                                    response.sendRedirect("/");
//                                })
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessHandler((request, response, authentication) -> {
                            HttpSession session = request.getSession(false);

                            if (authentication != null && authentication.getPrincipal() instanceof OAuth2User oAuth2User) {
                                Map<String, Object> attrs = oAuth2User.getAttributes();
                                Object provider = attrs.get("provider");

                                // [카카오 로그아웃 처리]
                                if ("KAKAO".equals(provider)) {
                                    String kakaoLogoutUrl = "https://kauth.kakao.com/oauth/logout?client_id="
                                            + KAKAO_CLIENT_ID
                                            + "&logout_redirect_uri=http://localhost:8080/";
                                    response.sendRedirect(kakaoLogoutUrl);
                                    if (session != null) session.invalidate();
                                    System.out.println("카카오 로그아웃 완료");
                                    return;
                                }

                                // [네이버 로그아웃 처리 + access token 만료]
                                if ("NAVER".equals(provider)) {
                                    if (session != null) {
                                        String accessToken = (String) session.getAttribute("NAVER_ACCESS_TOKEN");

                                        if (accessToken != null) {
                                            try {
                                                String revokeUrl = "https://nid.naver.com/oauth2.0/token?grant_type=delete"
                                                        + "&client_id=" + NAVER_CLIENT_ID
                                                        + "&client_secret=" + NAVER_CLIENT_SECRET
                                                        + "&access_token=" + accessToken
                                                        + "&service_provider=NAVER";

                                                RestTemplate rt = new RestTemplate();
                                                String result = rt.getForObject(revokeUrl, String.class);
                                                System.out.println("네이버 토큰 만료 결과: " + result);
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }
                                        }

                                        session.invalidate();
                                    }

                                    response.sendRedirect("https://nid.naver.com/nidlogin.logout");
                                    System.out.println("네이버 로그아웃 완료");
                                    return;
                                }
                            }

                            // 기본 로그아웃 처리
                            if (session != null) session.invalidate();
                            System.out.println("기본 로그아웃 완료");
                            response.sendRedirect("/");
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
//                        .authorizationEndpoint(authEndpoint -> authEndpoint
//                                .authorizationRequestResolver(
//                                        new CustomAuthorizationRequestResolver(
//                                                clientRegistrationRepository,
//                                                "/oauth2/authorization"
//                                        )
//                                )
//                        )
//                        .defaultSuccessUrl("/", true)
                        .userInfoEndpoint(user -> user.userService(customOAuth2UserService)) //
                        .successHandler(new OAuth2LoginSuccessHandler()) // 로그인 성공 시 세션 저장

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
