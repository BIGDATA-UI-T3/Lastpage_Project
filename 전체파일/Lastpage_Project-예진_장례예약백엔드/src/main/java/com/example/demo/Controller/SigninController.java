package com.example.demo.Controller;

import com.example.demo.Domain.Common.Dto.MemberDto;
import com.example.demo.Domain.Common.Entity.Member;
import com.example.demo.Domain.Common.Security.CustomUserPrincipal;
import com.example.demo.Domain.Common.Service.MemberService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/member")
public class SigninController {

    private final MemberService memberService;

    @PostMapping("/signin")
    public ResponseEntity<Map<String, Object>> signin(@RequestBody MemberDto dto, HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>();

        try {
            // 1️⃣ 회원 검증
            Member loginMember = memberService.signin(dto);
            if (loginMember == null) {
                throw new IllegalArgumentException("회원이 존재하지 않습니다.");
            }

            // 2️⃣ Spring Security 인증 정보 생성
            Map<String, Object> attributes = new HashMap<>();
            attributes.put("username", loginMember.getUsername());
            attributes.put("provider", "LOCAL");

            CustomUserPrincipal principal = new CustomUserPrincipal(loginMember, attributes);

            // 3️⃣ SecurityContextHolder에 등록 (로그인 상태 유지)
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
            org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(authentication);

            // ✅ 3-1️⃣ Spring Security 세션에 등록
            request.getSession().setAttribute("SPRING_SECURITY_CONTEXT",
                    org.springframework.security.core.context.SecurityContextHolder.getContext());

            // 4️⃣ HttpSession에 사용자 정보 저장
            HttpSession session = request.getSession(true);
            session.setAttribute("loginUser", loginMember);
            session.setMaxInactiveInterval(60 * 60); // 세션 1시간 유지

            System.out.println("✅ 자체 로그인 성공 및 SecurityContext 등록 완료: " + loginMember.getUsername());

            // 5️⃣ JSON 응답
            result.put("success", true);
            result.put("redirect", "/");
            result.put("username", loginMember.getUsername());
            return ResponseEntity.ok(result);

        } catch (IllegalArgumentException e) {
            result.put("success", false);
            result.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(result);

        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "서버 오류가 발생했습니다.");
            return ResponseEntity.internalServerError().body(result);
        }
    }

}
