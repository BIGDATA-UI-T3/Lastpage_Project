package com.example.demo.Domain.Common.Service;

import com.example.demo.Domain.Common.Dto.SignupDto;
import com.example.demo.Domain.Common.Entity.Signup;
import com.example.demo.Repository.SignupRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class SignupService {

    private final SignupRepository repository;
    private final PasswordEncoder passwordEncoder;

    /**
     * 회원가입 (일반 + 소셜 공통)
     */
    @Transactional
    public Signup saveUserInfo(SignupDto dto) {

        if (dto.getCreated_at() == null) dto.setCreated_at(LocalDateTime.now());
        dto.setUpdated_at(LocalDateTime.now());

        // 아이디 중복검사 (일반회원만)
        if (dto.getProvider() == null && existsById(dto.getId())) {
            throw new IllegalArgumentException("이미 사용 중인 아이디입니다.");
        }

        // 일반회원 비밀번호 검증
        if (dto.getProvider() == null) {
            validatePassword(dto.getPassword(), dto.getConfirm_password());
            dto.setPassword(passwordEncoder.encode(dto.getPassword()));
            dto.setConfirm_password(null);
        }

        Signup entity = Signup.builder()
                .name(dto.getName())
                .id(dto.getId())
                .password(dto.getPassword())
                .emailId(dto.getEmailId())
                .emailDomain(dto.getEmailDomain())
                .year(dto.getYear())
                .month(dto.getMonth())
                .day(dto.getDay())
                .gender(dto.getGender())
                .phone_num(dto.getPhone_num())
                .created_at(dto.getCreated_at())
                .updated_at(dto.getUpdated_at())
                .provider(dto.getProvider())
                .providerId(dto.getProviderId())
                .profileImage(dto.getProfileImage())
                .oauthEmail(dto.getOauthEmail())
                .build();

        if (dto.getProvider() == null)
            log.info("[일반 회원가입] ID={}, [UserSeq] = {}", dto.getId(),dto.getUserSeq());


        else
            log.info("[소셜 회원가입] Provider={}, ProviderId={}", dto.getProvider(), dto.getProviderId());

        log.info("저장 직전 userSeq={}", entity.getUserSeq());
        Signup saved = repository.save(entity);
        dto.setUserSeq(saved.getUserSeq());
        log.info("SERVICE 회원가입 완료! user_seq={}", saved.getUserSeq());
        return saved;
    }


    /**
     * 회원정보 삭제
     */
    public Signup deleteUserInfo(String userSeq, Signup Dto){
        Signup exisiting = repository.findById(userSeq)
                .orElseThrow(()-> new IllegalArgumentException("해당 회원을 찾을 수 없습니다. user_seq" + userSeq));

        log.info("[회원정보 삭제 시도] user_seq={}", userSeq);
return new Signup();
    }

    /**
     * 회원정보 수정 (로그인 사용자 기준)
     */
    public Signup updateUserInfo(String userSeq, SignupDto dto) {
        Signup existing = repository.findById(userSeq)
                .orElseThrow(() -> new IllegalArgumentException("해당 회원을 찾을 수 없습니다. user_seq=" + userSeq));

        log.info("[회원정보 수정 시도] user_seq={}", userSeq);

        // 비밀번호 변경 요청이 있을 경우만 처리
        if (dto.getPassword() != null && !dto.getPassword().trim().isEmpty()) {
            validatePassword(dto.getPassword(), dto.getConfirm_password());

            // 기존 비밀번호와 동일한지 확인
            if (passwordEncoder.matches(dto.getPassword(), existing.getPassword())) {
                throw new IllegalArgumentException("새 비밀번호는 기존 비밀번호와 달라야 합니다.");
            }

            existing.setPassword(passwordEncoder.encode(dto.getPassword()));
            log.info("[비밀번호 변경 완료] user_seq={}", userSeq);
        }

        // 변경 가능한 필드 업데이트
        existing.setName(dto.getName());
        existing.setPhone_num(dto.getPhone_num());
        existing.setGender(dto.getGender());
        existing.setYear(dto.getYear());
        existing.setMonth(dto.getMonth());
        existing.setDay(dto.getDay());
        existing.setUpdated_at(LocalDateTime.now());

        Signup saved = repository.save(existing);
        log.info("[회원정보 수정 완료] user_seq={}", userSeq);
        return saved;
    }

    /**
     * 비밀번호 유효성 검사 (대문자 ≥1, 특수문자 ≥1, 9자 이상)
     */
    private void validatePassword(String pw, String confirmPw) {
        if (pw == null || pw.trim().isEmpty()) {
            throw new IllegalArgumentException("비밀번호는 필수 입력값입니다.");
        }

        boolean valid = pw.length() >= 9 && pw.matches(".*[A-Z].*") && pw.matches(".*[!@#$%^&*].*");
        if (!valid) {
            throw new IllegalArgumentException("비밀번호는 대문자 1개 이상, 특수문자 1개 이상 포함, 최소 9자 이상이어야 합니다.");
        }

        if (confirmPw != null && !pw.equals(confirmPw)) {
            throw new IllegalArgumentException("비밀번호 확인이 일치하지 않습니다.");
        }
    }

    /**
     * 아이디 중복 검사
     */
    public boolean existsById(String id) {
        if (id == null || id.trim().isEmpty()) return false;
        return repository.findAll().stream()
                .anyMatch(u -> id.equals(u.getId()));
    }

    /**
     * 소셜 로그인 중복 확인
     */
    public Signup findByProviderAndProviderId(String provider, String providerId) {
        return repository.findAll().stream()
                .filter(u -> provider.equals(u.getProvider()) && providerId.equals(u.getProviderId()))
                .findFirst()
                .orElse(null);
    }
}
