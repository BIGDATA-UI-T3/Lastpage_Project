//package com.example.demo.Domain.Common.Service;
//
//import com.example.demo.Domain.Common.Entity.Member;
//import com.example.demo.Domain.Common.Security.CustomUserPrincipal;
//import com.example.demo.Kakao.KakaoUserInfo;
//import com.example.demo.Repository.MemberRepository;
//import lombok.RequiredArgsConstructor;
//import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
//import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
//import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
//import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
//import org.springframework.security.oauth2.core.user.OAuth2User;
//import org.springframework.stereotype.Service;
//
//import java.util.HashMap;
//import java.util.Map;
//
//@Service
//@RequiredArgsConstructor
//public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {
//
//    private final MemberRepository memberRepository;
//
//        @Override
//        public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
//            DefaultOAuth2UserService delegate = new DefaultOAuth2UserService();
//            OAuth2User oAuth2User = delegate.loadUser(userRequest);
//
//            // 카카오에서 받은 원본 attribute
//            Map<String, Object> attributes = oAuth2User.getAttributes();
//
//            // KakaoUserInfo로 파싱
//            KakaoUserInfo kakaoUserInfo = new KakaoUserInfo(attributes);
//
//            String id = kakaoUserInfo.getId();
//            String email = kakaoUserInfo.getEmail();
//            String name = kakaoUserInfo.getName();
//
//            // DB 저장 or 조회
//            Member member = memberRepository.findByEmail(email)
//                    .orElseGet(() -> memberRepository.save(
//                            Member.builder()
//                                    .name(name)
//                                    .email(email)
//                                    .username(email)
//                                    .password("KAKAO")
//                                    .provider("KAKAO")
//                                    .build()
//                    ));
//
//            // Thymeleaf에서도 바로 사용 가능한 형태로 attributes 구성
//            Map<String, Object> customAttributes = new HashMap<>();
//            customAttributes.put("id", id);
//            customAttributes.put("email", email);
//            customAttributes.put("ownerName", name); // 핵심!
//            customAttributes.put("provider", "KAKAO");
//
//            return new CustomUserPrincipal(member, customAttributes);
//        }
//    }

package com.example.demo.Domain.Common.Service;

import com.example.demo.Domain.Common.Entity.Member;
import com.example.demo.Domain.Common.Security.CustomUserPrincipal;
import com.example.demo.Kakao.KakaoUserInfo;
import com.example.demo.Repository.MemberRepository;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final MemberRepository memberRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        DefaultOAuth2UserService delegate = new DefaultOAuth2UserService();
        OAuth2User oAuth2User = delegate.loadUser(userRequest);

        String registrationId = userRequest.getClientRegistration().getRegistrationId(); // kakao / naver
        Map<String, Object> attributes = oAuth2User.getAttributes();

        String id, email, name;
        String provider;

        if ("kakao".equals(registrationId)) {
            KakaoUserInfo kakaoUserInfo = new KakaoUserInfo(attributes);
            id = kakaoUserInfo.getId();
            if (id == null || id.isEmpty()) {
                id = "KAKAO_" + System.currentTimeMillis();
            }
            email = kakaoUserInfo.getEmail();
            name = kakaoUserInfo.getName();
            provider = "KAKAO";

        } else if ("naver".equals(registrationId)) {
            Map<String, Object> response = (Map<String, Object>) attributes.get("response");
            if (response == null) response = new HashMap<>();

            email = String.valueOf(response.getOrDefault("email", ""));
            name = String.valueOf(response.getOrDefault("name", "")); // 필수 제공 정보 기준

            // null 방지용
            id = (email == null || email.isEmpty()) ? "NAVER_" + System.currentTimeMillis() : email;

//            // 이메일이 없으면 임시 id 생성
//            if (email == null || email.isEmpty()) {
//                id = "NAVER_" + System.currentTimeMillis();
//            } else {
//                id = email;
//            }
            provider = "NAVER";

        } else {
            throw new OAuth2AuthenticationException("지원하지 않는 OAuth provider: " + registrationId);
        }

        // DB 저장 or 조회
        Member member = memberRepository.findByEmail(email)
                .orElseGet(() -> memberRepository.save(
                        Member.builder()
                                .name(name)
                                .email(email)
                                .username(email)
                                .password(provider) // KAKAO / NAVER 구분
                                .provider(provider)
                                .build()
                ));
        // 현재 세션 가져오기
        HttpSession session = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes())
                .getRequest().getSession(true);

        // 네이버 로그인일 경우 access token 저장
        if ("NAVER".equals(provider)) {
            String accessToken = userRequest.getAccessToken().getTokenValue();
            session.setAttribute("NAVER_ACCESS_TOKEN", accessToken);
        }

        // 공통 로그인 정보 세션 저장
        session.setAttribute("LOGIN_USER", Map.of(
                "provider", provider,
                "id", id,
                "email", email,
                "name", name
        ));

        // 세션 또는 Thymeleaf 사용 attributes
        Map<String, Object> customAttributes = new HashMap<>();
        customAttributes.put("id", id);
        customAttributes.put("email", email);
        customAttributes.put("ownerName", name);
        customAttributes.put("provider", provider);

        return new CustomUserPrincipal(member, customAttributes);
    }
}

