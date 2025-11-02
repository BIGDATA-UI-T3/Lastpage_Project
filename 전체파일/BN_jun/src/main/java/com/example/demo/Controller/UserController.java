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

import java.security.Principal; // [추가]
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

    @PostMapping("/signup")
    public String processRegister(
            @Valid @ModelAttribute("registerFormDto") RegisterFormDto dto,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            HttpSession session) {

        if (bindingResult.hasErrors()) {
            return "signup";
        }
        if (!dto.getPassword().equals(dto.getPasswordCheck())) {
            bindingResult.addError(new FieldError("registerFormDto", "passwordCheck", "비밀번호가 일치하지 않습니다."));
            return "signup";
        }
        // 이메일 인증 코드 검증 로직
        String sessionCode = (String) session.getAttribute("emailVerificationCode");
        String sessionEmail = (String) session.getAttribute("emailForVerification");
        String userCode = dto.getSmsCode();
        String userEmail = dto.getEmailId() + "@" + dto.getEmailDomain();

        if (sessionCode == null || userCode == null || userCode.isBlank()) {
            bindingResult.addError(new FieldError("registerFormDto", "smsCode", "이메일 인증을 먼저 진행해주세요."));
            return "signup";
        }
        if (sessionEmail == null || !sessionEmail.equals(userEmail)) {
            bindingResult.addError(new FieldError("registerFormDto", "emailId", "인증을 요청한 이메일 주소와 다릅니다."));
            return "signup";
        }
        if (!sessionCode.equals(userCode)) {
            bindingResult.addError(new FieldError("registerFormDto", "smsCode", "인증 코드가 올바르지 않습니다."));
            return "signup";
        }

        try {
            userService.registerUser(dto);
            session.invalidate();
        } catch (IllegalStateException e) {

            String field = e.getMessage().contains("아이디") ? "username" : "emailId";
            bindingResult.addError(new FieldError("registerFormDto", field, e.getMessage()));
            return "signup";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "회원가입 중 오류가 발생했습니다: " + e.getMessage());
            return "redirect:/signup";
        }

        redirectAttributes.addFlashAttribute("successMessage", "회원가입이 완료되었습니다! 로그인해주세요.");
        return "redirect:/signin";
    }

    // 마이페이지 로직
    @GetMapping("/mypage")
    public String showMyPage(Model model, Principal principal) {

        // [principal.getName()으로 현재 로그인한 사용자의 username을 가져옴
        String username = principal.getName();

        // 모든 예약을 가져오는 대신, '해당 사용자의 예약'만 가져옴
        List<GoodsReserve> reservationList = goodsReserveService.getAllGoodsReservationsByUsername(username);

        model.addAttribute("goodsReservationList", reservationList);

        return "mypage";
    }
}