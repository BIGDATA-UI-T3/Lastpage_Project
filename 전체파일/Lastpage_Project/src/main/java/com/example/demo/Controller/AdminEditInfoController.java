package com.example.demo.Controller;

import com.example.demo.Domain.Common.Dto.SignupDto;
import com.example.demo.Domain.Common.Entity.Signup;
import com.example.demo.Domain.Common.Service.EditInfoService;
import com.example.demo.Domain.Common.Service.SignupService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

//Repo -> EditInfoService -> Controller -> <-js->html
@Controller
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/admin/user")
public class AdminEditInfoController {

    private final EditInfoService editInfoService;
    private final SignupService signupService;
    /** ------------------------------------------
     *  회원정보 수정 페이지 진입 (관리자 모드)
     *  GET /admin/user/edit/{userSeq}
     * ------------------------------------------ */
    @GetMapping("/edit/{userSeq}")
    public String adminEditPage(@PathVariable String userSeq, Model model) {

        // 수정할 회원 정보 읽기
        SignupDto userInfo = editInfoService.getUserInfo(userSeq);

        model.addAttribute("user", userInfo);
        model.addAttribute("userType", "admin");   // ★ 관리자 모드 전달
        model.addAttribute("targetUserSeq", userSeq);

        log.info("[ADMIN] 회원 수정 페이지 접근 → userSeq={}", userSeq);

        return "mypage/EditInfo";  // 동일한 EditInfo.html 재사용
    }

    /** ------------------------------------------
     *  회원정보 수정 처리 (관리자 모드)
     *  PUT /admin/user/update/{userSeq}
     * ------------------------------------------ */
    @PutMapping("/update/{userSeq}")
    @ResponseBody
    public ResponseEntity<?> adminUpdateUser(
            @PathVariable String userSeq,
            @RequestBody SignupDto dto
    ) {
        try {
            // 관리자 모드에서는 userType = "admin"
            Signup updatedUser = editInfoService.updateUserInfo(userSeq, dto, "admin");

            log.info("[ADMIN] 회원정보 수정 완료 → userSeq={}, name={}", updatedUser.getUserSeq(), updatedUser.getName());
            return ResponseEntity.ok("관리자가 회원 정보를 수정했습니다.");

        } catch (Exception e) {
            log.error("[ADMIN] 회원정보 수정 오류", e);
            return ResponseEntity.internalServerError().body("회원정보 수정 중 오류가 발생했습니다.");
        }
    }

    /** ------------------------------------------
     *  회원 삭제 처리 (관리자 모드)
     *  DELETE /admin/user/{userSeq}
     * ------------------------------------------ */
    @DeleteMapping("/{userSeq}")
    @ResponseBody
    public ResponseEntity<?> adminDeleteUser(@PathVariable String userSeq) {
        try {
            editInfoService.deleteUserInfo(userSeq);
            log.info("[ADMIN] 회원 삭제 완료 → userSeq={}", userSeq);
            return ResponseEntity.ok("회원이 삭제되었습니다.");
        } catch (Exception e) {
            log.error("[ADMIN] 회원 삭제 오류", e);
            return ResponseEntity.internalServerError().body("회원 삭제 중 오류가 발생했습니다.");
        }
    }
    /* =======================================================
       (4) 관리자 세션 강제 갱신 API
           - JS에서 필요할 때 호출 가능
    ======================================================= */
    @PostMapping("/session/refresh")
    @ResponseBody
    public ResponseEntity<?> refreshAdminSession(HttpSession session) {

        Signup loginUser = (Signup) session.getAttribute("loginUser");

        // 로그인한 사람이 관리자라면 세션 갱신
        if (loginUser != null) {
            Signup refreshed = signupService.findById(loginUser.getUserSeq());
            session.setAttribute("loginUser", refreshed);

            log.info("[ADMIN] 관리자 세션 리프레시 완료 → userSeq={}", refreshed.getUserSeq());
        }

        return ResponseEntity.ok("session-refreshed");
    }



}
