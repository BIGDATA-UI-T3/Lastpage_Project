package com.example.demo.Controller;


import com.example.demo.Domain.Common.Dto.PsyReserveDto;
import com.example.demo.Domain.Common.Dto.SignupDto;
import com.example.demo.Domain.Common.Entity.PsyReserve;
import com.example.demo.Domain.Common.Entity.Signup;
import com.example.demo.Domain.Common.Service.FuneralReserveService;
import com.example.demo.Domain.Common.Service.SignupService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller

@Slf4j
@RequestMapping
@RequiredArgsConstructor
public class SigninController {

    private final SignupService signupService;


    @GetMapping("/signin")//http://localhost:8099/signin
    public String home(){
        System.out.println("GET /");
        return "signin/Signin";
    }

    @PostMapping("/loginProc")
    public String login(@RequestParam String id,
                        @RequestParam String password,
                        HttpSession session,
                        RedirectAttributes redirectAttributes) {
        try {
            Signup user = signupService.authenticate(id, password);
            session.setAttribute("loginUser", user);
            session.setAttribute("loginEmail", user.getEmailId());
            log.info("[로그인 성공] Id={}, Email={}", user.getId(), user.getEmailId());
            return "redirect:/"; // 로그인 성공 시 메인 페이지로
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            log.warn("[로그인 실패] {}", e.getMessage());
            return "redirect:/signin";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }

// 이거는 회원가입 하면서 이제 필요 없잖아
//    @PostMapping("/userinfoSave")
//    @ResponseBody
//    public ResponseEntity<?> saveUserInfo(@RequestBody SignupDto dto) {
//        try {
//            Signup saved = signupService.saveUserInfo(dto);
//            log.info("유저 정보 저장 완료!! 굳굳: {}", saved.getId());
//
//            // JS에서 redirect할 수 있도록 email을 응답에 담아줌
//            return ResponseEntity.ok(saved.getId());
//        } catch (Exception e) {
//            log.error(" 유저 정보 저장 실패!! 그만 실패해 진짜 짜증나게 하지마", e);
//            return ResponseEntity.internalServerError().body("유저 정보 저장 실패!! 그만 실패해 제발진짜그만");
//        }
//    }


}
