package com.example.demo.Controller;

import com.example.demo.Domain.Common.Service.OAuthService;
import com.example.demo.Domain.Common.Dto.SignupDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.view.RedirectView;

@Slf4j
@Controller
@RequestMapping("/login/oauth2")
@RequiredArgsConstructor
public class LoginController {

    private final OAuthService oAuthService;

    /** 🔹 카카오 로그인 콜백 */
    @GetMapping("/code/kakao")
    public RedirectView kakaoCallback(@RequestParam String code) {
        log.info("카카오 로그인 요청 code={}", code);
        SignupDto user = oAuthService.loginWithKakao(code);

        // 로그인 성공 후 리디렉션
        RedirectView redirectView = new RedirectView();
        redirectView.setUrl("/signin/SigninSuccess?user=");
        return redirectView;
    }

    /** 🔹 네이버 로그인 콜백 */
    @GetMapping("/code/naver")
    public RedirectView naverCallback(
            @RequestParam String code,
            @RequestParam String state) {
        log.info("네이버 로그인 요청 code={}, state={}", code, state);
        SignupDto user = oAuthService.loginWithNaver(code, state);

        RedirectView redirectView = new RedirectView();
        redirectView.setUrl("/signin/SigninSuccess?user=");
        return redirectView;
    }

    /** 🔹 구글 로그인 콜백 */
    @GetMapping("/code/google")
    public RedirectView googleCallback(@RequestParam String code) {
        log.info("구글 로그인 요청 code={}", code);
        SignupDto user = oAuthService.loginWithGoogle(code);

        RedirectView redirectView = new RedirectView();
        redirectView.setUrl("/signin/SigninSuccess?user=");
        return redirectView;
    }
}
