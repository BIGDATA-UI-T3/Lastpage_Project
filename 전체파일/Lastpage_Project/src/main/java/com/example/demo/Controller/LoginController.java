package com.example.demo.Controller;

import com.example.demo.Domain.Common.Dto.SignupDto;
import com.example.demo.Domain.Common.Service.OAuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Slf4j
@Controller
@RequestMapping("/login/oauth2")
@RequiredArgsConstructor
public class LoginController {

    private final OAuthService oAuthService;

    /* ======================================================
     * 1) 카카오 로그인 콜백
     * ====================================================== */
    @GetMapping("/code/kakao")
    public RedirectView kakaoCallback(
            @RequestParam String code,
            HttpSession session,
            HttpServletRequest request,
            HttpServletResponse response) {

        log.info("카카오 로그인 요청 code={}", code);

        SignupDto user = oAuthService.loginWithKakao(code);

        saveSessionLogin(session, user);
        setSecurityAuthentication(user, request, response);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        log.info("AUTH 저장됨 = {}", auth);

        String name = user.getName() != null ? user.getName() : "사용자";
        return new RedirectView("/?welcome=" + URLEncoder.encode(name, StandardCharsets.UTF_8));
    }

    /* ======================================================
     * 2) 네이버 로그인 콜백
     * ====================================================== */
    @GetMapping("/code/naver")
    public RedirectView naverCallback(
            @RequestParam String code,
            @RequestParam String state,
            HttpSession session,
            HttpServletRequest request,
            HttpServletResponse response) {

        log.info("네이버 로그인 요청 code={}, state={}", code, state);

        SignupDto user = oAuthService.loginWithNaver(code, state);

        saveSessionLogin(session, user);
        setSecurityAuthentication(user, request, response);

        String name = user.getName() != null ? user.getName() : "사용자";
        return new RedirectView("/?welcome=" + URLEncoder.encode(name, StandardCharsets.UTF_8));
    }

    /* ======================================================
     * 3) 구글 로그인 콜백
     * ====================================================== */
    @GetMapping("/code/google")
    public RedirectView googleCallback(
            @RequestParam String code,
            HttpSession session,
            HttpServletRequest request,
            HttpServletResponse response) {

        log.info("구글 로그인 요청 code={}", code);

        SignupDto user = oAuthService.loginWithGoogle(code);

        saveSessionLogin(session, user);
        setSecurityAuthentication(user, request, response);

        String name = user.getName() != null ? user.getName() : "사용자";
        return new RedirectView("/?welcome=" + URLEncoder.encode(name, StandardCharsets.UTF_8));
    }

    /* ======================================================
     * 공통: 세션 저장
     * ====================================================== */
    private void saveSessionLogin(HttpSession session, SignupDto user) {
        session.setAttribute("userSeq", user.getUserSeq());
        session.setAttribute("loginUser", user);
        session.setAttribute("loginEmail", user.getOauthEmail());
        session.setAttribute("loginName", user.getName());
        session.setAttribute("loginRole", user.getRole());

        log.info("세션 저장 완료 userSeq={}, role={}", user.getUserSeq(), user.getRole());
    }

    /* ======================================================
     * 공통: SecurityContext 인증 저장 (가장 중요)
     * ====================================================== */
    private void setSecurityAuthentication(
            SignupDto userDto,
            HttpServletRequest request,
            HttpServletResponse response) {

        // role 문자열 정제 ("ROLE_USER" → "USER")
        String rawRole = (userDto.getRole() != null) ? userDto.getRole() : "ROLE_USER";

        String roleName = rawRole.startsWith("ROLE_")
                ? rawRole.substring(5) // USER
                : rawRole;

        // UserDetails 생성
        UserDetails userDetails = org.springframework.security.core.userdetails.User
                .withUsername(userDto.getUserSeq())
                .password("SOCIAL_LOGIN")
                .roles(roleName) // USER 또는 ADMIN
                .build();

        Authentication auth =
                new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );

        // SecurityContext 생성하고 인증 저장
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);

        // ★★ 가장 중요한 부분: SecurityContext를 세션에 강제로 저장
        HttpSessionSecurityContextRepository repo = new HttpSessionSecurityContextRepository();
        repo.saveContext(context, request, response);

        log.info("[SecurityContext 저장 완료] userSeq={}, role={}", userDto.getUserSeq(), roleName);
    }
}
