package com.example.demo.Domain.Common.Service;

import com.example.demo.Domain.Common.Dto.SignupDto;
import com.example.demo.Domain.Common.Entity.Signup;
import com.example.demo.Repository.SignupRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class SignupService {

    private final SignupRepository repository;
    private final PasswordEncoder passwordEncoder;

    /**
     * 회원 저장 (일반 + 소셜 공통)
     */
    public Signup saveUserInfo(SignupDto dto) {

        // 생성/수정 시각 기본값 설정
        if (dto.getCreated_at() == null) dto.setCreated_at(LocalDateTime.now());
        dto.setUpdated_at(LocalDateTime.now());

        // 아이디 중복 검사 (일반 회원만)
        if (dto.getProvider() == null && existsById(dto.getId())) {
            throw new IllegalArgumentException("이미 사용 중인 아이디입니다.");
        }

        // 이메일 중복 검사 (선택 비활성)
        // String fullEmail = dto.getEmailId() + "@" + dto.getEmailDomain();
        // if (existsByEmail(fullEmail)) {
        //     throw new IllegalArgumentException("이미 가입된 이메일입니다.");
        // }

        // 비밀번호 검증 (인코딩 전에)
        if (dto.getProvider() == null) {
            String pw = dto.getPassword() != null ? dto.getPassword().trim() : "";
            String cpw = dto.getConfirm_password() != null ? dto.getConfirm_password().trim() : "";

            if (pw.isEmpty()) {
                throw new IllegalArgumentException("비밀번호는 필수 입력값입니다.");
            }

            // 대문자 ≥1, 특수문자 ≥1, 9자 이상
            boolean valid = pw.length() >= 9 && pw.matches(".*[A-Z].*") && pw.matches(".*[!@#$%^&*].*");
            if (!valid) {
                throw new IllegalArgumentException("비밀번호는 대문자 1개 이상, 특수문자 1개 이상 포함, 최소 9자 이상이어야 합니다.");
            }

            // 비밀번호 확인 비교 (암호화 전)
            if (!pw.equals(cpw)) {
                throw new IllegalArgumentException("비밀번호 확인이 일치하지 않습니다.");
            }

            // 검증 완료 후 암호화 수행
            dto.setPassword(passwordEncoder.encode(pw));
            dto.setConfirm_password(null);
        }

        // 엔티티 변환
        Signup entity = Signup.builder()
                .name(dto.getName())
                .id(dto.getId())
                .password(dto.getPassword())  // 이제 인코딩된 값이 들어감
                .confirm_password(null)
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
                .profileImage(dto.getProfileImage())
                .oauthEmail(dto.getOauthEmail())
                .build();

        // 일반/소셜 구분 로그
        if (dto.getProvider() == null) {
            log.info("[일반 회원가입] ID: {}", dto.getId());
        } else {
            log.info("[소셜 회원가입] Provider: {} / ProviderId: {}", dto.getProvider(), dto.getProviderId());
            entity.setPassword(null);
            entity.setConfirm_password(null);
        }

        // DB 저장
        Signup saved = repository.save(entity);
        log.info("회원가입 완료! user_seq = {}", saved.getUser_seq());
        return saved;
    }

    /** 아이디 중복 검사 */
    public boolean existsById(String id) {
        if (id == null || id.trim().isEmpty()) return false;
        return repository.findAll().stream()
                .anyMatch(u -> id.equals(u.getId()));
    }

    /** 소셜 로그인 중복 확인 */
    public Signup findByProviderAndProviderId(String provider, String providerId) {
        return repository.findAll().stream()
                .filter(u -> provider.equals(u.getProvider()) && providerId.equals(u.getProviderId()))
                .findFirst()
                .orElse(null);
    }
}
