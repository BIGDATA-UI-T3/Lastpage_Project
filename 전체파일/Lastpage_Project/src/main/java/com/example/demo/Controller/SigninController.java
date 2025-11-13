package com.example.demo.Controller;

import com.example.demo.Domain.Common.Entity.Signup;
import com.example.demo.Domain.Common.Service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
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

            //  세션에 모든 로그인 정보 저장 (공통 구조)
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
    public ResponseEntity<Map<String, Object>> loginRest(
            @RequestBody Map<String, String> request,
            HttpSession session,
            HttpServletRequest httpRequest) {

        String id = request.get("id");
        String password = request.get("password");

        try {
            // 기존 세션 무효화 (보안 강화)
            if (session != null) {
                session.invalidate();
            }

            // 새 세션 생성
            HttpSession newSession = httpRequest.getSession(true);

            // 사용자 인증
            Signup user = authService.authenticate(id, password);

            //  세션 등록 (SSR 방식과 동일하게 통일)
            newSession.setAttribute("loginUser", user);
            newSession.setAttribute("userSeq", user.getUserSeq());
            newSession.setAttribute("loginEmail", user.getEmailId());
            newSession.setAttribute("loginName", user.getName());

            // 응답 데이터 구성
            Map<String, Object> response = new HashMap<>();
            response.put("result", "ok");
            response.put("message", "로그인 성공");
            response.put("user", Map.of(
                    "userSeq", user.getUserSeq(),
                    "id", user.getId(),
                    "name", user.getName()
            ));

            log.info("[REST 로그인 성공] Id={}, userSeq={}", user.getId(), user.getUserSeq());
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.warn("[REST 로그인 실패] {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of(
                            "result", "fail",
                            "message", e.getMessage()
                    ));
        } catch (Exception e) {
            log.error("[REST 로그인 오류]", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of(
                            "result", "error",
                            "message", "서버 오류가 발생했습니다."
                    ));
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
