package com.example.demo.Controller;

import com.example.demo.Domain.Common.Dto.SignupDto;
import com.example.demo.Domain.Common.Service.EmailService;
import com.example.demo.Domain.Common.Service.SignupService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Controller
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/signup")
public class SignupController {

    private final SignupService signupService;
    private final EmailService emailService;

    /** 회원가입 페이지 */
    @GetMapping
    public String signupPage() {
        return "signup/Signup";
    }

    /** ID 중복검사 */
    @ResponseBody
    @GetMapping("/checkDuplicateId")
    public ResponseEntity<String> checkDuplicateId(@RequestParam String id) {
        boolean exists = signupService.existsById(id);
        return ResponseEntity.ok(exists ? "duplicate" : "ok");
    }

    /** 이메일 인증번호 전송 */
    @ResponseBody
    @PostMapping("/sendEmailCode")
    public ResponseEntity<String> sendEmailCode(@RequestBody Map<String, String> req) throws Exception {
        emailService.sendAuthCode(req.get("email"));
        return ResponseEntity.ok("sent");
    }

    /** 이메일 인증번호 확인 */
    @ResponseBody
    @PostMapping("/verifyEmailCode")
    public ResponseEntity<String> verifyEmailCode(@RequestBody Map<String, String> req) {
        boolean result = emailService.verifyCode(req.get("email"), req.get("code"));
        return ResponseEntity.ok(result ? "success" : "fail");
    }

    /** 회원가입 저장 */
    @ResponseBody
    @PostMapping("/userinfoSave")
    public ResponseEntity<?> userinfoSave(
            @RequestBody SignupDto dto,
            HttpSession session) {

        try {
            log.info("[회원가입 요청] {}", dto);

            // 1) 회원정보 저장 (Signup → SignupDto 반환)
            SignupDto savedUser = signupService.saveUserInfo(dto);

            // 2) 세션 저장 (SignupDto로 통일)
            session.setAttribute("loginUser", savedUser);
            session.setAttribute("userSeq", savedUser.getUserSeq());
            session.setAttribute("loginName", savedUser.getName());
            session.setAttribute("loginRole", savedUser.getRole());

            // 3) Spring Security 인증 처리
            applySecurityAuthentication(savedUser);

            return ResponseEntity.ok(savedUser.getUserSeq());

        } catch (Exception e) {
            log.error("회원가입 오류", e);
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /** Security 인증 처리 */
    private void applySecurityAuthentication(SignupDto user) {

        // Security는 "USER" 형태를 받고 → ROLE_USER 자동 생성함
        String role = (user.getRole() != null) ? user.getRole() : "USER";

        User userDetails = (User) User.withUsername(user.getUserSeq())
                .password(user.getPassword() != null ? user.getPassword() : "SOCIAL_LOGIN")
                .roles(role)   // ← "USER" 넣으면 "ROLE_USER" 자동 생성
                .build();

        Authentication auth =
                new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities()
                );

        SecurityContextHolder.getContext().setAuthentication(auth);

        log.info("[Security 회원가입 인증 완료] userSeq={}, role={}",
                user.getUserSeq(), role);
    }
}
