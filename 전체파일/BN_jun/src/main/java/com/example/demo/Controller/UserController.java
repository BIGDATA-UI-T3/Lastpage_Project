package com.example.demo.Controller;

import com.example.demo.Domain.Common.Dto.RegisterFormDto;
import com.example.demo.Domain.Common.Entity.GoodsReserve;
import com.example.demo.Domain.Common.Service.GoodsReserveService;
import com.example.demo.Domain.Common.Service.UserService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final GoodsReserveService goodsReserveService;


    @GetMapping("/signup")
    public String showRegisterPage(Model model) {
        model.addAttribute("registerFormDto", new RegisterFormDto());
        return "signup";
    }

    // 회원가입 처리 (유효성 검사 + 이메일 인증)
    @PostMapping("/signup")
    public String processRegister(
            // 1. @Valid: DTO의 유효성 검사(예: @NotBlank)를 실행
            // 2. @ModelAttribute: 폼 데이터를 DTO에 바인딩
            @Valid @ModelAttribute("registerFormDto") RegisterFormDto dto,
            // 3. BindingResult: @Valid의 검사 결과 (성공/실패)
            BindingResult bindingResult,
            // 4. RedirectAttributes: 회원가입 성공/실패 메시지 전달
            RedirectAttributes redirectAttributes,
            // 5. HttpSession: 이메일 인증 코드를 검증하기 위해 세션 사용
            HttpSession session) {

        // 검사1) DTO에 정의된 유효성 검사하기 (@NotBlank, @Size, @Pattern...)
        if (bindingResult.hasErrors()) {
            // 검사 실패 시 입력했던 데이터와 오류 메시지를 가지고 signup.html 페이지로 돌아가게
            return "signup";
        }

        // 검사2) 비밀번호와 비밀번호 확인이 일치하는지
        if (!dto.getPassword().equals(dto.getPasswordCheck())) {
            // bindingResult에 "passwordCheck" 필드 오류를 수동으로 추가
            bindingResult.addError(new FieldError("registerFormDto", "passwordCheck", "비밀번호가 일치하지 않습니다."));
            return "signup";
        }

        // 검사 3) 이메일 인증 코드가 일치하는지
        String sessionCode = (String) session.getAttribute("emailVerificationCode");
        String sessionEmail = (String) session.getAttribute("emailForVerification");
        String userCode = dto.getSmsCode();
        String userEmail = dto.getEmailId() + "@" + dto.getEmailDomain();

        // [오류 3-1] 인증을 아예 안 한 경우
        if (sessionCode == null || userCode == null || userCode.isBlank()) {
            bindingResult.addError(new FieldError("registerFormDto", "smsCode", "이메일 인증을 먼저 진행해주세요."));
            return "signup";
        }

        // [오류 3-2] 인증받은 이메일과 폼에 적힌 이메일이 다른 경우
        if (sessionEmail == null || !sessionEmail.equals(userEmail)) {
            bindingResult.addError(new FieldError("registerFormDto", "emailId", "인증을 요청한 이메일 주소와 다릅니다."));
            return "signup";
        }

        // [오류 3-3] 인증 코드가 틀린 경우
        if (!sessionCode.equals(userCode)) {
            bindingResult.addError(new FieldError("registerFormDto", "smsCode", "인증 코드가 올바르지 않습니다."));
            return "signup";
        }

        // 검사 4) 모든 검증 통과
        try {
            // DTO를 UserService로 넘겨서 DB에 저장
            userService.registerUser(dto);

            // 인증 성공 시, 세션에 저장된 코드 정보 즉시 삭제
            session.invalidate();

        } catch (IllegalStateException e) {
            // [오류 4-1] 아이디 중복 등 UserService에서 발생한 오류
            bindingResult.addError(new FieldError("registerFormDto", "username", e.getMessage()));
            return "signup";
        } catch (Exception e) {
            // [오류 4-2] 그 외 알 수 없는 오류 (DB 연결 오류 등)
            redirectAttributes.addFlashAttribute("errorMessage", "회원가입 중 오류가 발생했습니다: " + e.getMessage());
            return "redirect:/signup";
        }

        // 회원가입 완료
        redirectAttributes.addFlashAttribute("successMessage", "회원가입이 완료되었습니다! 로그인해주세요.");
        return "redirect:/signin";
    }

    // SecurityConfig에 의해 로그인되지 않은 사용자는 /signin으로 튕겨냄
    @GetMapping("/mypage")
    public String showMyPage(Model model) {

        List<GoodsReserve> reservationList = goodsReserveService.getAllGoodsReservations();

        model.addAttribute("goodsReservationList", reservationList);

        return "mypage";
    }
}