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

            //  서비스 호출 → 수정된 영속 객체 반환
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
}
