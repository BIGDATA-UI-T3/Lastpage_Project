package com.example.demo.Controller;

import com.example.demo.Domain.Common.Dto.SignupDto;
import com.example.demo.Domain.Common.Service.OAuthService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.view.RedirectView;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Slf4j
@Controller
@RequestMapping("/login/oauth2")
@RequiredArgsConstructor
public class LoginController {

    private final OAuthService oAuthService;



    /**  카카오 로그인 콜백 */
    @GetMapping("/code/kakao")
    public RedirectView kakaoCallback(@RequestParam String code, HttpSession session) {
        log.info("카카오 로그인 요청 code={}", code);
        SignupDto user = oAuthService.loginWithKakao(code);

        //  세션 저장
        session.setAttribute("userSeq", user.getUserSeq());
        log.info("userSeq={}", user.getUserSeq());
        session.setAttribute("loginUser", user);
        session.setAttribute("loginEmail", user.getOauthEmail());
        session.setAttribute("loginName", user.getName());


        String name = user.getName() != null ? user.getName() : "사용자";
        String redirectUrl = "/?welcome=" + URLEncoder.encode(name, StandardCharsets.UTF_8);
        log.info("카카오 로그인 완료 → redirect: {}", redirectUrl);

        return new RedirectView(redirectUrl);
        //  리디렉트 URL
//        String name = user.getName() != null ? user.getName() : "사용자";
//        String redirectUrl = "/?login=success&user=" +
//                URLEncoder.encode(name, StandardCharsets.UTF_8);
//        log.info("카카오 로그인 성공 → 세션 저장 완료 / 리디렉트: {}", redirectUrl);

//        return new RedirectView(redirectUrl);
    }

    /**  네이버 로그인 콜백 */
    @GetMapping("/code/naver")
    public RedirectView naverCallback(@RequestParam String code,
                                      @RequestParam String state,
                                      HttpSession session) {
        log.info("네이버 로그인 요청 code={}, state={}", code, state);
        SignupDto user = oAuthService.loginWithNaver(code, state);

        //  세션 저장
        session.setAttribute("userSeq", user.getUserSeq());
        session.setAttribute("loginUser", user);
        session.setAttribute("loginEmail", user.getOauthEmail());
        session.setAttribute("loginName", user.getName());

        //  리디렉트 URL
        String name = user.getName() != null ? user.getName() : "사용자";
        String redirectUrl = "/?welcome=" + URLEncoder.encode(name, StandardCharsets.UTF_8);
        log.info("네이버 로그인 완료 → redirect: {}", redirectUrl);

        return new RedirectView(redirectUrl);


    }

    /**  구글 로그인 콜백 */
    @GetMapping("/code/google")
    public RedirectView googleCallback(@RequestParam String code, HttpSession session) {
        log.info("구글 로그인 요청 code={}", code);
        SignupDto user = oAuthService.loginWithGoogle(code);

        //  세션 저장
        session.setAttribute("userSeq", user.getUserSeq());
        session.setAttribute("loginUser", user);
        session.setAttribute("loginEmail", user.getOauthEmail());
        session.setAttribute("loginName", user.getName());

        //  리디렉트 URL
        String name = user.getName() != null ? user.getName() : "사용자";
        String redirectUrl = "/?welcome=" + URLEncoder.encode(name, StandardCharsets.UTF_8);
        log.info("구글 로그인 완료 → redirect: {}", redirectUrl);

        return new RedirectView(redirectUrl);

    }
}
