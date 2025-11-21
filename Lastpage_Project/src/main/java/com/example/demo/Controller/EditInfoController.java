package com.example.demo.Controller;

import com.example.demo.Domain.Common.Dto.SignupDto;
import com.example.demo.Domain.Common.Entity.Signup;
import com.example.demo.Domain.Common.Service.EditInfoService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Controller
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/mypage/EditInfo")
public class EditInfoController {

    private final EditInfoService editInfoService;

    /** ------------------------------------------
     *  회원정보 수정 페이지 진입
     * ------------------------------------------ */
    @GetMapping
    public String editInfoPage(@SessionAttribute(value = "loginUser", required = false) Object loginUser,
                               Model model) {
        if (loginUser == null) {
            log.warn("비로그인 사용자의 접근 시도 → /signin 리다이렉트");
            return "redirect:/signin";
        }

        String userType;
        String userSeq;

        // 세션 구분 (자체 vs 소셜)
        if (loginUser instanceof SignupDto dto) {
            userType = "social";
            userSeq = dto.getUserSeq();
        } else if (loginUser instanceof Signup entity) {
            userType = "native";
            userSeq = entity.getUserSeq();
        } else {
            log.error("잘못된 세션 객체 유형");
            return "redirect:/signin";
        }

        // 회원정보 불러오기
        var userInfo = editInfoService.getUserInfo(userSeq);
        model.addAttribute("user", userInfo);
        model.addAttribute("userType", userType);

        log.info("[회원정보 수정 페이지 접근] userSeq={}, userType={}", userSeq, userType);
        return "mypage/EditInfo";
    }

    /** ------------------------------------------
     *  회원정보 수정 처리 (영속성 기반 + 세션 즉시 갱신)
     * ------------------------------------------ */
    @PutMapping("/update")
    @ResponseBody
    public ResponseEntity<?> updateMember(@SessionAttribute(value = "loginUser", required = false) Object loginUser,
                                          @RequestBody SignupDto dto,
                                          HttpSession session) {
        try {
            if (loginUser == null) {
                return ResponseEntity.status(401).body("로그인이 필요합니다.");
            }

            String userSeq;
            String userType;

            if (loginUser instanceof SignupDto socialUser) {
                userSeq = socialUser.getUserSeq();
                userType = "social";
            } else if (loginUser instanceof Signup nativeUser) {
                userSeq = nativeUser.getUserSeq();
                userType = "native";
            } else {
                return ResponseEntity.badRequest().body("유효하지 않은 로그인 세션입니다.");
            }

            // 서비스 호출 → 수정된 영속 객체 반환
            Signup updatedUser = editInfoService.updateUserInfo(userSeq, dto, userType);

            // 세션 즉시 갱신 (DB 재조회 없음)
            session.setAttribute("loginUser", updatedUser);

            log.info("[회원정보 수정 완료 + 세션 갱신] userSeq={}, name={}", updatedUser.getUserSeq(), updatedUser.getName());
            return ResponseEntity.ok(updatedUser.getName());

        } catch (IllegalArgumentException e) {
            log.warn("회원정보 수정 실패: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            log.error("회원정보 수정 중 오류 발생", e);
            return ResponseEntity.internalServerError().body("회원정보 수정 중 오류가 발생했습니다.");
        }
    }

    /** ------------------------------------------
     *  비밀번호 재사용 검사 (AJAX 실시간 유효성 체크용)
     * ------------------------------------------ */
    @PostMapping("/checkPassword")
    @ResponseBody
    public ResponseEntity<?> checkPasswordReuse(@SessionAttribute(value = "loginUser", required = false) Object loginUser,
                                                @RequestBody String newPassword) {
        try {
            if (loginUser == null) {
                return ResponseEntity.status(401).body("로그인이 필요합니다.");
            }

            // 로그인 사용자 식별
            String userSeq = (loginUser instanceof Signup s)
                    ? s.getUserSeq()
                    : ((SignupDto) loginUser).getUserSeq();

            boolean reused = editInfoService.checkPasswordReuse(userSeq, newPassword);
            return ResponseEntity.ok(reused ? "reused" : "ok");
        } catch (Exception e) {
            log.error("비밀번호 재사용 검사 오류", e);
            return ResponseEntity.internalServerError().body("검사 중 오류 발생");
        }
    }

    /** ------------------------------------------
     *  세션 유효성 확인 (/mypage/EditInfo/session/check)
     * ------------------------------------------ */
    @GetMapping("/session/check")
    @ResponseBody
    public ResponseEntity<?> checkSession(
            @SessionAttribute(value = "loginUser", required = false) Object loginUser) {

        if (loginUser == null) {
            log.warn("[SESSION] 세션이 존재하지 않음 (로그아웃 상태)");
            return ResponseEntity.status(401).body("세션이 만료되었습니다.");
        }

        Map<String, Object> response = new HashMap<>();

        if (loginUser instanceof SignupDto user) {
            response.put("userType", "social");
            response.put("userSeq", user.getUserSeq());
            response.put("name", user.getName());
            log.info("[SESSION] 소셜 로그인 사용자 확인 userSeq={}", user.getUserSeq());
        } else if (loginUser instanceof Signup user) {
            response.put("userType", "native");
            response.put("userSeq", user.getUserSeq());
            response.put("name", user.getName());
            log.info("[SESSION] 자체 로그인 사용자 확인 userSeq={}", user.getUserSeq());
        } else {
            log.error("[SESSION] 잘못된 세션 객체 유형: {}", loginUser.getClass().getName());
            return ResponseEntity.badRequest().body("유효하지 않은 세션입니다.");
        }

        return ResponseEntity.ok(response);
    }

    /** ------------------------------------------
     *  비밀번호 검증 (탈퇴 전 확인용)
     * ------------------------------------------ */
    @PostMapping("/verify-password/{userSeq}")
    @ResponseBody
    public ResponseEntity<?> verifyPassword(
            @PathVariable("userSeq") String userSeq,
            @RequestBody String password) {

        try {
            boolean valid = editInfoService.verifyPassword(userSeq, password);
            if (valid) {
                log.info("[비밀번호 검증 성공] userSeq={}", userSeq);
                return ResponseEntity.ok("valid");
            } else {
                log.warn("[비밀번호 검증 실패] userSeq={}", userSeq);
                return ResponseEntity.ok("invalid");
            }
        } catch (Exception e) {
            log.error("비밀번호 검증 중 오류 발생", e);
            return ResponseEntity.internalServerError().body("비밀번호 검증 중 오류가 발생했습니다.");
        }
    }

    /** ------------------------------------------
     *  회원탈퇴 처리 (소셜/자체 공통)
     * ------------------------------------------ */
    @DeleteMapping("/delete/{userSeq}")
    @ResponseBody
    public ResponseEntity<?> deleteUser(
            @PathVariable("userSeq") String userSeq,
            HttpSession session,
            @SessionAttribute(value = "loginUser", required = false) Object loginUser) {

        if (loginUser == null) {
            log.warn("[회원탈퇴] 세션이 존재하지 않음 → 로그인 필요");
            return ResponseEntity.status(401).body("세션이 만료되었습니다. 다시 로그인해주세요.");
        }

        try {
            // 세션에서 로그인 타입 확인
            boolean isSocial = loginUser instanceof SignupDto;

            // 소셜 로그인 사용자는 비밀번호 확인 없이 바로 탈퇴
            if (isSocial) {
                editInfoService.deleteUserInfo(userSeq);
                session.invalidate();
                log.info("[회원탈퇴 완료] 소셜 로그인 userSeq={}", userSeq);
                return ResponseEntity.ok("소셜 로그인 회원 탈퇴가 완료되었습니다.");
            }

            // 자체 로그인은 JS 쪽에서 verify-password 확인을 거침
            editInfoService.deleteUserInfo(userSeq);
            session.invalidate();
            log.info("[회원탈퇴 완료] 자체 로그인 userSeq={}", userSeq);
            return ResponseEntity.ok("회원 탈퇴가 완료되었습니다.");

        } catch (Exception e) {
            log.error("[회원탈퇴 오류]", e);
            return ResponseEntity.internalServerError().body("회원 탈퇴 중 오류가 발생했습니다.");
        }
    }
}
