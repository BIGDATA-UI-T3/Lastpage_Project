package com.example.demo.Controller;

import com.example.demo.Domain.Common.Dto.SignupDto;
import com.example.demo.Domain.Common.Entity.Signup;
import com.example.demo.Domain.Common.Service.EmailService;
import com.example.demo.Domain.Common.Service.SignupService;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Controller
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/signup")
public class SignupController {

    private final SignupService signupService;
    private final EmailService emailService;

    /** 회원가입 페이지 이동 (GET) */
    @GetMapping
    public String signupPage() {
        log.info("GET /signup → signup/Signup.html 페이지 호출");
        return "signup/Signup";
    }

    /** 아이디 중복 검사 */
    @ResponseBody
    @GetMapping("/checkDuplicateId")
    public ResponseEntity<String> checkDuplicateId(@RequestParam String id) {
        log.info("아이디 중복검사 요청: {}", id);
        boolean exists = signupService.existsById(id);
        return ResponseEntity.ok(exists ? "duplicate" : "ok");
    }

    /** 이메일 인증번호 전송 */
    @ResponseBody
    @PostMapping("/sendEmailCode")
    public ResponseEntity<String> sendEmailCode(@RequestBody Map<String, String> req) throws MessagingException {
        String email = req.get("email");
        log.info("이메일 인증 요청: {}", email);
        emailService.sendAuthCode(email);
        return ResponseEntity.ok("sent");
    }

    /** 이메일 인증번호 확인 */
    @ResponseBody
    @PostMapping("/verifyEmailCode")
    public ResponseEntity<String> verifyEmailCode(@RequestBody Map<String, String> req) {
        String email = req.get("email");
        String code = req.get("code");
        boolean result = emailService.verifyCode(email, code);
        log.info("이메일 인증 확인: {} → {}", email, result ? "성공" : "실패");
        return ResponseEntity.ok(result ? "success" : "fail");
    }

    @ResponseBody
    @PostMapping("/userinfoSave")
    public ResponseEntity<String> userinfoSave(@RequestBody SignupDto dto,
                                               HttpSession session) { // 세션 주입 추가
        log.info("받은 회원가입 요청: {}", dto);
        try {
            if (dto.getProvider() == null) {
                log.info("[일반 회원가입 요청] ID: {}", dto.getId());

                String pw = dto.getPassword() != null ? dto.getPassword().trim() : "";
                String cpw = dto.getConfirm_password() != null ? dto.getConfirm_password().trim() : "";

                if (pw.isEmpty()) {
                    return ResponseEntity.badRequest().body("비밀번호는 필수 입력값입니다.");
                }

                boolean valid = pw.length() >= 9 && pw.matches(".*[A-Z].*") && pw.matches(".*[!@#$%^&*].*");
                if (!valid) {
                    return ResponseEntity.badRequest().body("비밀번호는 대문자 1개 이상, 특수문자 1개 이상 포함, 최소 9자 이상이어야 합니다.");
                }

                if (!pw.equals(cpw)) {
                    return ResponseEntity.badRequest().body("비밀번호 확인이 일치하지 않습니다.");
                }
            } else {
                log.info("[소셜 회원가입 요청] Provider: {} / ProviderId: {}", dto.getProvider(), dto.getProviderId());
            }

            // 회원정보 저장
            Signup saved = signupService.saveUserInfo(dto);
            log.info("회원가입 완료! user_seq={}", saved.getUserSeq());

            //  세션에 로그인 사용자 저장 (자동 로그인 효과)
            session.setAttribute("loginUser", saved);
            log.info("세션 등록 완료 → {}", saved.getName());

            return ResponseEntity.ok(saved.getUserSeq());

        } catch (IllegalArgumentException e) {
            log.warn("회원가입 검증 실패: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());

        } catch (Exception e) {
            log.error("회원가입 처리 중 오류 발생", e);
            return ResponseEntity.internalServerError().body("회원가입 실패");
        }
    }
}
