package com.example.demo.Controller;

import com.example.demo.Domain.Common.Entity.Signup;
import com.example.demo.Domain.Common.Service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@Slf4j
@RequiredArgsConstructor
public class SigninController {

    private final AuthService authService;

    /** 로그인 페이지 이동 */
    @GetMapping("/signin")
    public String signinPage() {
        log.info("로그인 페이지 이동");
        return "signin/Signin";
    }

    /** -------------------------------
     *  자체 로그인 (SSR)
     * ------------------------------- */
    @PostMapping("/loginProc")
    public String login(@RequestParam String id,
                        @RequestParam String password,
                        HttpSession session,
                        HttpServletRequest request,
                        HttpServletResponse response,
                        RedirectAttributes redirectAttributes) {

        try {
            Signup user = authService.authenticate(id, password);

            // 1) 세션 저장
            session.setAttribute("loginUser", user);
            session.setAttribute("userSeq", user.getUserSeq());
            session.setAttribute("loginEmail", user.getEmailId());
            session.setAttribute("loginName", user.getName());
            session.setAttribute("loginRole", user.getRole().name());  // USER 또는 ADMIN

            // 2) Spring Security 인증 등록
            UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken(
                            user,
                            null,
                            List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
                    );

// SecurityContext 저장
            SecurityContextHolder.getContext().setAuthentication(auth);

// ★★★ 세션에도 저장해야 진짜 로그인 유지됨 ★★★
            session.setAttribute("SPRING_SECURITY_CONTEXT", SecurityContextHolder.getContext());


            log.info("[LOGIN SUCCESS] id={}, role={}", user.getId(), user.getRole().name());
            log.info("AUTH = {}", auth);

            return "redirect:/";

        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/signin";
        }
    }

    /** -------------------------------
     *  자체 로그인 (REST API)
     * ------------------------------- */
    @PostMapping("/api/loginProc")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> loginRest(
            @RequestBody Map<String, String> request,
            HttpSession session,
            HttpServletRequest httpServletRequest,
            HttpServletResponse httpServletResponseresponse) {

        String id = request.get("id");
        String password = request.get("password");

        try {
            if (session != null) session.invalidate();
            HttpSession newSession = httpServletRequest.getSession(true);

            Signup user = authService.authenticate(id, password);

            // 세션 저장
            newSession.setAttribute("loginUser", user);
            newSession.setAttribute("userSeq", user.getUserSeq());
            newSession.setAttribute("loginEmail", user.getEmailId());
            newSession.setAttribute("loginName", user.getName());
            newSession.setAttribute("loginRole", user.getRole().name());

            // Security 인증
            UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(
                            user,
                            null,
                            List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
                    );

            SecurityContextHolder.getContext().setAuthentication(authToken);
            newSession.setAttribute("SPRING_SECURITY_CONTEXT", SecurityContextHolder.getContext());


            // 응답
            Map<String, Object> response = new HashMap<>();
            response.put("result", "ok");
            response.put("message", "로그인 성공");
            response.put("user", Map.of(
                    "userSeq", user.getUserSeq(),
                    "id", user.getId(),
                    "name", user.getName()
            ));

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("[REST LOGIN ERROR]", e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("result", "fail", "message", e.getMessage()));
        }
    }

    /** 로그아웃 */
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        SecurityContextHolder.clearContext();
        log.info("로그아웃 완료");
        return "redirect:/";
    }
}
