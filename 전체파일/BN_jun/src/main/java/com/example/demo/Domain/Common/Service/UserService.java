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
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error; // [추가]
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    // [기능 1] 자체 회원가입 (DTO 버전)
    public User registerUser(RegisterFormDto dto) {
        if (userRepository.findByUsername(dto.getUsername()).isPresent()) {
            throw new IllegalStateException("이미 존재하는 아이디입니다.");
        }

        // [추가] 이메일 중복 검사
        String fullEmail = dto.getEmailId() + "@" + dto.getEmailDomain();
        if (userRepository.findByEmail(fullEmail).isPresent()) {
            throw new IllegalStateException("이미 사용 중인 이메일입니다.");
        }

        User user = new User();
        user.setName(dto.getName());
        user.setUsername(dto.getUsername());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setEmail(fullEmail); // [수정] 조합된 이메일 저장
        user.setPhone(dto.getPhone());
        user.setRole("ROLE_USER");

        return userRepository.save(user);
    }

    // [기능 2] 자체 로그인 (수정 필요 없음)
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // ... (기존 로직 동일) ...
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

    // [기능 3] 소셜 로그인 (카카오 + 네이버 + 구글)
    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

        OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate = new DefaultOAuth2UserService();
        OAuth2User oAuth2User = delegate.loadUser(userRequest);

        String provider = userRequest.getClientRegistration().getRegistrationId();
        String providerId = null;
        String email = null;
        String name = null;

        if (provider.equals("google")) {
            providerId = oAuth2User.getAttribute("sub");
            email = oAuth2User.getAttribute("email");
            name = oAuth2User.getAttribute("name");
        } else if (provider.equals("kakao")) {
            providerId = oAuth2User.getAttribute("id").toString();
            Map<String, Object> kakaoAccount = oAuth2User.getAttribute("kakao_account");
            email = (String) kakaoAccount.get("email");
            Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");
            name = (String) profile.get("nickname");
        } else if (provider.equals("naver")) {
            Map<String, Object> response = (Map<String, Object>) oAuth2User.getAttribute("response");
            providerId = (String) response.get("id");
            email = (String) response.get("email");
            name = (String) response.get("name");
        }

        String username = provider + "_" + providerId; // 소셜 로그인 전용 username

        Optional<User> userOptional = userRepository.findByUsername(username);
        User user;

        if (userOptional.isPresent()) {
            // 1. 이미 해당 소셜 계정으로 로그인한 적이 있음
            user = userOptional.get();
        } else {
            // 2. 해당 소셜 계정으로는 처음 로그인
            // [추가] 이메일이 이미 존재하는지 확인
            if (email != null && userRepository.findByEmail(email).isPresent()) {
                // 3. 이메일이 이미 존재 (자체 회원가입 또는 다른 소셜 로그인)
                // -> 로그인 실패 처리
                OAuth2Error error = new OAuth2Error("duplicate_email",
                        "이미 가입된 이메일입니다. 다른 로그인 방식을 이용해주세요.", null);
                throw new OAuth2AuthenticationException(error, error.toString());
            }

            // 4. 신규 사용자로 DB에 저장
            user = new User();
            user.setUsername(username);
            user.setPassword(passwordEncoder.encode("")); // 소셜 로그인은 비밀번호 사용 안 함
            user.setName(name);
            user.setEmail(email);
            user.setRole("ROLE_USER");
            userRepository.save(user);
        }

        // ... (이하 기존 로직 동일) ...
        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority(user.getRole()));

        Map<String, Object> attributes = oAuth2User.getAttributes();
        String userNameAttributeName;

        if (provider.equals("naver")) {
            userNameAttributeName = "response";
        } else if (provider.equals("kakao")) {
            userNameAttributeName = "id";
        } else {
            userNameAttributeName = "sub";
        }

        return new DefaultOAuth2User(
                authorities,
                attributes,
                userNameAttributeName
        );
    }
}