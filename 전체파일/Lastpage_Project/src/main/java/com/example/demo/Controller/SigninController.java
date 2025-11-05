package com.example.demo.Controller;

import com.example.demo.Domain.Common.Entity.Signup;
import com.example.demo.Domain.Common.Service.AuthService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.Map;

@Controller
@Slf4j
@RequiredArgsConstructor
public class SigninController {

    private final AuthService authService;

    /** -------------------------------
     *  로그인 페이지 이동
     * ------------------------------- */
    @GetMapping("/signin")
    public String signinPage() {
        log.info("로그인 페이지로 이동합니다.");
        return "signin/Signin";
    }

    /** -------------------------------
     *  자체 로그인 (form submit - SSR 방식)
     * ------------------------------- */
    @PostMapping("/loginProc")
    public String login(@RequestParam String id,
                        @RequestParam String password,
                        HttpSession session,
                        RedirectAttributes redirectAttributes) {
        try {
            Signup user = authService.authenticate(id, password);

            // 세션에 모든 로그인 정보 저장
            session.setAttribute("loginUser", user);
            session.setAttribute("userSeq", user.getUserSeq());
            session.setAttribute("loginEmail", user.getEmailId());
            session.setAttribute("loginName", user.getName());

            log.info("[로그인 성공] Id={}, userSeq={}, Email={}", user.getId(), user.getUserSeq(), user.getEmailId());

            return "redirect:/";  // 로그인 후 메인으로 이동

        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            log.warn("[로그인 실패] {}", e.getMessage());
            return "redirect:/signin";
        }
    }

    /** -------------------------------
     *  자체 로그인 (REST API - JS fetch용)
     * ------------------------------- */
    @PostMapping("/api/loginProc")
    @ResponseBody
    public ResponseEntity<?> loginRest(@RequestBody Map<String, String> request, HttpSession session) {
        String id = request.get("id");
        String password = request.get("password");

        try {
            Signup user = authService.authenticate(id, password);

            // 세션에도 동일하게 저장 (서버 렌더링 페이지 접근 가능)
            session.setAttribute("loginUser", user);
            session.setAttribute("userSeq", user.getUserSeq());
            session.setAttribute("loginEmail", user.getEmailId());
            session.setAttribute("loginName", user.getName());

            Map<String, Object> response = new HashMap<>();
            response.put("result", "ok");
            response.put("userSeq", user.getUserSeq());
            response.put("id", user.getId());
            response.put("name", user.getName());
            response.put("email", user.getEmailId());

            log.info("[REST 로그인 성공] Id={}, userSeq={}", user.getId(), user.getUserSeq());
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.warn("[REST 로그인 실패] {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("result", "fail", "message", e.getMessage()));
        }
    }

    /** -------------------------------
     *  로그아웃 처리
     * ------------------------------- */
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        log.info("로그아웃 완료");
        return "redirect:/";
    }
}
