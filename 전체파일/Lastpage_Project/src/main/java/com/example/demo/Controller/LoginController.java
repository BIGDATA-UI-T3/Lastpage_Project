package com.example.demo.Controller;

import com.example.demo.Domain.Common.Service.OAuthService;
import com.example.demo.Domain.Common.Dto.SignupDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
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

    /**카카오 로그인 콜백 */
    @GetMapping("/code/kakao")
    public RedirectView kakaoCallback(@RequestParam String code) {
        log.info("카카오 로그인 요청 code={}", code);
        SignupDto user = oAuthService.loginWithKakao(code);

        // 이름 전달 시 URL 인코딩 처리 (한글 깨짐 방지)
        String name = user.getName() != null ? user.getName() : "사용자"; // name이 들어오면 getName()으로 name초기화하고 아니면 디폴트 값 "사용자가 뜰 것"
        String encodedName = URLEncoder.encode(name, StandardCharsets.UTF_8);
        String redirectUrl = "/mypage/Mypage?user=" + encodedName;// mypage로 넘어갈 때 넘어온 한글 인코딩 된 name을 함께 넘겨줍니다.
        log.info("카카오 로그인 성공 → 리디렉트: {}", redirectUrl);// 이건 그냥 로그로 확인
        return new RedirectView(redirectUrl);



    }



    /** 네이버 로그인 콜백 */
    @GetMapping("/code/naver")
    public RedirectView naverCallback(
            @RequestParam String code,
            @RequestParam String state) {
        log.info("네이버 로그인 요청 code={}, state={}", code, state);
        SignupDto user = oAuthService.loginWithNaver(code, state);
        String name = user.getName() != null ? user.getName() : "사용자";
        String redirectUrl = "/mypage/Mypage?user=" + URLEncoder.encode(name, StandardCharsets.UTF_8);
        return new RedirectView(redirectUrl);
    }

    /** 구글 로그인 콜백 */
    @GetMapping("/code/google")
    public RedirectView googleCallback(@RequestParam String code) {
        log.info("구글 로그인 요청 code={}", code);
        SignupDto user = oAuthService.loginWithGoogle(code);
        String name = user.getName() != null ? user.getName() : "사용자";
        String redirectUrl = "/mypage/Mypage?user=" + URLEncoder.encode(name, StandardCharsets.UTF_8);
        return new RedirectView(redirectUrl);
    }
}
