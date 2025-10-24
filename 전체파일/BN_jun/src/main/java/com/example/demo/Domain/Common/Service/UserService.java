package com.example.demo.Domain.Common.Service; // 👈 1. 패키지 선언

// ▼▼▼ 2. 누락된 import문 모두 추가 ▼▼▼
import com.example.demo.Domain.Common.Dto.RegisterFormDto;
import com.example.demo.Domain.Common.Entity.User; // 👈 [수정] 'User' 엔티티 import
import com.example.demo.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails; // 👈 [수정] UserDetails import
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService; // 👈 [수정]
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException; // 👈 [수정]
import org.springframework.security.oauth2.core.user.DefaultOAuth2User; // 👈 [수정]
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // 👈 [수정] @Transactional import

import java.util.ArrayList; // 👈 [수정]
import java.util.List; // 👈 [수정]
import java.util.Map; // 👈 [수정]
import java.util.Optional; // 👈 [수정]
// ▲▲▲ import문 끝 ▲▲▲

@Service
@RequiredArgsConstructor
@Transactional // 👈 이제 이 어노테이션을 인식함
public class UserService implements UserDetailsService, OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // [기능 1] 자체 회원가입 (DTO 버전)
    public User registerUser(RegisterFormDto dto) {
        if (userRepository.findByUsername(dto.getUsername()).isPresent()) {
            throw new IllegalStateException("이미 존재하는 아이디입니다.");
        }

        User user = new User(); // 👈 이제 'User' 엔티티를 인식함
        user.setName(dto.getName());
        user.setUsername(dto.getUsername());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setEmail(dto.getEmailId() + "@" + dto.getEmailDomain());
        user.setPhone(dto.getPhone());

        // (DTO의 생년월일, 성별 등도 User 엔티티에 필드가 있다면 여기서 set 해줘야 함)
        // user.setBirthdate(dto.getBirthYear() + ...);
        // user.setGender(dto.getGender());

        user.setRole("ROLE_USER");
        return userRepository.save(user);
    }

    // [기능 2] 자체 로그인 (수정 없음)
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
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

    // [기능 3] 소셜 로그인 (카카오 + 네이버 + 구글) (수정 없음)
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

        String username = provider + "_" + providerId;

        Optional<User> userOptional = userRepository.findByUsername(username);
        User user;

        if (userOptional.isPresent()) {
            user = userOptional.get();
        } else {
            user = new User();
            user.setUsername(username);
            user.setPassword(passwordEncoder.encode("")); // 비밀번호 없음
            user.setName(name);
            user.setEmail(email);
            user.setRole("ROLE_USER");
            userRepository.save(user);
        }

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