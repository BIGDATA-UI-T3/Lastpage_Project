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
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.HashMap; // [추가] HashMap import

@Service
@RequiredArgsConstructor
@Transactional
public class UserService implements UserDetailsService, OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public User registerUser(RegisterFormDto dto) {
        if (userRepository.findByUsername(dto.getUsername()).isPresent()) {
            throw new IllegalStateException("이미 존재하는 아이디입니다.");
        }
        String fullEmail = dto.getEmailId() + "@" + dto.getEmailDomain();
        if (userRepository.findByEmail(fullEmail).isPresent()) {
            throw new IllegalStateException("이미 사용 중인 이메일입니다.");
        }
        User user = new User();
        user.setName(dto.getName());
        user.setUsername(dto.getUsername());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setEmail(fullEmail);
        user.setPhone(dto.getPhone());
        user.setRole("ROLE_USER");
        return userRepository.save(user);
    }

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

    // 소셜 로그인 (카카오 + 네이버 + 구글)
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
            // 1. 이미 해당 소셜 계정으로 로그인한 적이 있음
            user = userOptional.get();
        } else {
            // 2. 해당 소셜 계정으로는 처음 로그인
            Optional<User> existingUserOptional = (email != null) ? userRepository.findByEmail(email) : Optional.empty();

            if (existingUserOptional.isPresent()) {
                // 3. 이메일이 이미 존재 (자체 회원가입 또는 다른 소셜 로그인)
                User existingUser = existingUserOptional.get();
                String providerName = getProviderNameFromUsername(existingUser.getUsername());
                String errorMessage = String.format(
                        "%s로 이미 가입된 이메일입니다. %s로 로그인해주세요.",
                        providerName, providerName
                );
                OAuth2Error error = new OAuth2Error("duplicate_email", errorMessage, null);
                throw new OAuth2AuthenticationException(error, error.toString());
            }

            // 4. 신규 사용자로 DB에 저장
            user = new User();
            user.setUsername(username);
            user.setPassword(passwordEncoder.encode(""));
            user.setName(name);
            user.setEmail(email);
            user.setRole("ROLE_USER");
            userRepository.save(user); // save(user)로 변경하여 user 객체에 ID가 할당되도록 함
        }

        // 1. 권한 설정
        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority(user.getRole()));

        // 2. attributes 맵을 복사하고, '우리의' username을 저장합니다.
        Map<String, Object> attributes = new HashMap<>(oAuth2User.getAttributes());
        attributes.put("internal_username", user.getUsername()); // "naver_...", "kakao_...", etc.

        // 3. 'name' 속성의 키를 우리가 방금 저장한 "internal_username"으로 지정합니다.
        //    이렇게 하면 principal.getName()이 항상 'user.getUsername()'을 반환하게 됩니다.
        String userNameAttributeName = "internal_username";

        // 4. 수정된 attributes와 "internal_username" 키를 사용하여 DefaultOAuth2User 반환
        return new DefaultOAuth2User(
                authorities,
                attributes,
                userNameAttributeName
        );
    }

    // 사용자 아이디를 기반으로 가입 방식을 반환하는 헬퍼 메서드
    private String getProviderNameFromUsername(String username) {
        if (username == null) {
            return "정보 없음";
        }
        if (username.startsWith("kakao_")) {
            return "카카오";
        } else if (username.startsWith("naver_")) {
            return "네이버";
        } else if (username.startsWith("google_")) {
            return "구글";
        } else {
            return "자체";
        }
    }
}