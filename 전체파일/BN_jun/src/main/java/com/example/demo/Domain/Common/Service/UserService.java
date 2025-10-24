package com.example.demo.Domain.Common.Service;

import com.example.demo.Domain.Common.Dto.RegisterFormDto;
import com.example.demo.Domain.Common.Entity.User;
import com.example.demo.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService implements UserDetailsService, OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // [기능 1] 자체 회원가입 (수정 없음)
    public User registerUser(RegisterFormDto dto) {
        // ... (이전 코드와 동일) ...
        if (userRepository.findByUsername(dto.getUsername()).isPresent()) {
            throw new IllegalStateException("이미 존재하는 아이디입니다.");
        }
        if (!dto.getPassword().equals(dto.getPasswordCheck())) {
            throw new IllegalStateException("비밀번호가 일치하지 않습니다.");
        }
        User user = new User();
        user.setName(dto.getName());
        user.setUsername(dto.getUsername());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setEmail(dto.getEmailId() + "@" + dto.getEmailDomain());
        user.setPhone(dto.getPhone());
        user.setRole("ROLE_USER");
        return userRepository.save(user);
    }

    // [기능 2] 자체 로그인 (수정 없음)
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // ... (이전 코드와 동일) ...
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("아이디를 찾을 수 없습니다: " + username));
        List<GrantedAuthority> authorities = new ArrayList<>();
        if (user.getRole() != null) {
            authorities.add(new SimpleGrantedAuthority(user.getRole()));
        }
        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                authorities
        );
    }

    // ▼▼▼ [기능 3] 소셜 로그인 처리 (카카오 + 네이버 + 구글) ▼▼▼
    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

        OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate = new DefaultOAuth2UserService();
        OAuth2User oAuth2User = delegate.loadUser(userRequest);

        String provider = userRequest.getClientRegistration().getRegistrationId();

        String providerId = null;
        String email = null;
        String name = null;

        if (provider.equals("google")) {
            // [구글 파싱]
            providerId = oAuth2User.getAttribute("sub"); // 구글의 고유 ID
            email = oAuth2User.getAttribute("email");
            name = oAuth2User.getAttribute("name");

        } else if (provider.equals("kakao")) {
            // [카카오 파싱]
            providerId = oAuth2User.getAttribute("id").toString();
            Map<String, Object> kakaoAccount = oAuth2User.getAttribute("kakao_account");
            email = (String) kakaoAccount.get("email");
            Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");
            name = (String) profile.get("nickname");

        } else if (provider.equals("naver")) {
            // [네이버 파싱]
            Map<String, Object> response = (Map<String, Object>) oAuth2User.getAttribute("response");
            providerId = (String) response.get("id");
            email = (String) response.get("email");
            name = (String) response.get("name");
        }

        // [중요] 우리 서비스의 고유 아이디 생성
        String username = provider + "_" + providerId;

        // 이미 가입한 사용자인지 확인
        Optional<User> userOptional = userRepository.findByUsername(username);
        User user;

        if (userOptional.isPresent()) {
            // 이미 가입한 경우 -> 로그인 성공
            user = userOptional.get();
        } else {
            // 첫 소셜 로그인인 경우 -> 강제 회원가입
            user = new User();
            user.setUsername(username);
            user.setPassword(passwordEncoder.encode("")); // 비밀번호 없음
            user.setName(name);
            user.setEmail(email);
            user.setRole("ROLE_USER");
            userRepository.save(user);
        }

        // Spring Security가 사용할 UserDetails 객체로 반환
        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority(user.getRole()));

        Map<String, Object> attributes = oAuth2User.getAttributes();
        String userNameAttributeName;

        if (provider.equals("naver")) {
            userNameAttributeName = "response"; // 네이버는 "response"
        } else if (provider.equals("kakao")) {
            userNameAttributeName = "id"; // 카카오는 "id"
        } else {
            userNameAttributeName = "sub"; // 구글은 "sub"
        }

        return new DefaultOAuth2User(
                authorities,
                attributes,
                userNameAttributeName
        );
    }
}