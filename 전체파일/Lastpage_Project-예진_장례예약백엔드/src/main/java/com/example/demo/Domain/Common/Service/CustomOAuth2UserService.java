package com.example.demo.Domain.Common.Service;

import com.example.demo.Domain.Common.Entity.Member;
import com.example.demo.Domain.Common.Security.CustomUserPrincipal;
import com.example.demo.Kakao.KakaoUserInfo;
import com.example.demo.Repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

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

            // 카카오에서 받은 원본 attribute
            Map<String, Object> attributes = oAuth2User.getAttributes();

            // KakaoUserInfo로 파싱
            KakaoUserInfo kakaoUserInfo = new KakaoUserInfo(attributes);

            String id = kakaoUserInfo.getId();
            String email = kakaoUserInfo.getEmail();
            String name = kakaoUserInfo.getName();

            // DB 저장 or 조회
            Member member = memberRepository.findByEmail(email)
                    .orElseGet(() -> memberRepository.save(
                            Member.builder()
                                    .name(name)
                                    .email(email)
                                    .username(email)
                                    .password("KAKAO")
                                    .provider("KAKAO")
                                    .build()
                    ));

            // Thymeleaf에서도 바로 사용 가능한 형태로 attributes 구성
            Map<String, Object> customAttributes = new HashMap<>();
            customAttributes.put("id", id);
            customAttributes.put("email", email);
            customAttributes.put("ownerName", name); // 핵심!
            customAttributes.put("provider", "KAKAO");

            return new CustomUserPrincipal(member, customAttributes);
        }
    }


