package com.example.demo.Domain.Common.Service;

import com.example.demo.Domain.Common.Dto.SignupDto;
import com.example.demo.Domain.Common.Entity.Signup;
import com.example.demo.Repository.SignupRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class SignupService {

    private final SignupRepository repository;

    /**
     * ✅ 회원 정보 저장
     * 일반 회원가입 & 소셜 회원가입 모두 처리 가능
     */
    public Signup saveUserInfo(SignupDto dto) {

        // 생성일시 기본값 설정
        if (dto.getCreated_at() == null) dto.setCreated_at(LocalDateTime.now());
        dto.setUpdated_at(LocalDateTime.now());

        Signup entity = Signup.builder()
                .name(dto.getName())
                .id(dto.getId())
                .password(dto.getPassword())
                .confirm_password(dto.getConfirm_password())
                .emailId(dto.getEmailId())
                .emailDomain(dto.getEmailDomain())
                .year(dto.getYear())
                .month(dto.getMonth())
                .day(dto.getDay())
                .gender(dto.getGender())
                .phone_num(dto.getPhone_num())
                .sms_auth_number(dto.getSms_auth_number())
                .created_at(dto.getCreated_at())
                .updated_at(dto.getUpdated_at())
                .provider(dto.getProvider())
                .providerId(dto.getProviderId())
                .profile_image(dto.getProfileImage())
                .oauth_email(dto.getOauthEmail())
                .build();

        // ✅ 일반회원/소셜회원 구분 로직
        if (dto.getProvider() == null) {
            // 일반 회원가입
            log.info("📦 일반 회원가입: {}", dto.getId());
            if (dto.getPassword() == null || dto.getPassword().isEmpty()) {
                throw new IllegalArgumentException("비밀번호는 필수 입력값입니다.");
            }
        } else {
            // 소셜 회원가입
            log.info("🌐 소셜 회원가입 [{}]: {}", dto.getProvider(), dto.getOauthEmail());
            entity.setPassword(null);  // 소셜 로그인은 비밀번호 불필요
            entity.setConfirm_password(null);
        }

        Signup saved = repository.save(entity);
        log.info("✅ 회원정보 저장 완료! user_seq = {}", saved.getUser_seq());
        return saved;
    }

    /**
     * ✅ 이메일 중복, 소셜 회원 중복 방지용 유틸 (선택사항)
     */
    public boolean existsByEmail(String email) {
        return repository.findAll().stream()
                .anyMatch(u -> (u.getEmailId() + "@" + u.getEmailDomain()).equals(email));
    }

    /**
     * ✅ 소셜 로그인 시 이미 등록된 회원인지 확인
     */
    public Signup findByProviderAndProviderId(String provider, String providerId) {
        return repository.findAll().stream()
                .filter(u -> provider.equals(u.getProvider()) && providerId.equals(u.getProviderId()))
                .findFirst()
                .orElse(null);
    }
}
