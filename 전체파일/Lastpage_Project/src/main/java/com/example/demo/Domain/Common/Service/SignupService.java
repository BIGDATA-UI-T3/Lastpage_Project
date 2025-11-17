package com.example.demo.Domain.Common.Service;

import com.example.demo.Domain.Common.Dto.SignupDto;
import com.example.demo.Domain.Common.Entity.Gender;
import com.example.demo.Domain.Common.Entity.Role;
import com.example.demo.Domain.Common.Entity.Signup;
import com.example.demo.Repository.SignupRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SignupService {

    private final SignupRepository repository;
    private final PasswordEncoder passwordEncoder;


    /** ======================================================
     *   회원가입 (일반 + 소셜 공통)
     * ====================================================== */
    @Transactional
    public SignupDto saveUserInfo(SignupDto dto) {

        if (dto.getCreated_at() == null) dto.setCreated_at(LocalDateTime.now());
        dto.setUpdated_at(LocalDateTime.now());

        boolean isSocial = dto.getProvider() != null;

        /* ------------------------------------------------------
         *  일반 회원가입일 경우 비밀번호 검증 + 암호화 처리
         * ------------------------------------------------------ */
        if (!isSocial) {

            // 아이디 중복 검사
            if (existsById(dto.getId())) {
                throw new IllegalArgumentException("이미 사용 중인 아이디입니다.");
            }

            validatePassword(dto.getPassword(), dto.getConfirm_password());
            dto.setPassword(passwordEncoder.encode(dto.getPassword()));
            dto.setConfirm_password(null);
        }

        /* ------------------------------------------------------
         *  Gender Enum 변환 (안전 처리)
         * ------------------------------------------------------ */
        Gender genderValue = null;
        if (dto.getGender() != null) {
            try {
                genderValue = Gender.valueOf(dto.getGender());
            } catch (IllegalArgumentException e) {
                genderValue = Gender.N; // 기본값
            }
        }


        /* ------------------------------------------------------
         *  엔티티 생성
         * ------------------------------------------------------ */
        Signup entity = Signup.builder()
                .name(dto.getName())
                .id(dto.getId())
                .password(dto.getPassword())    // 소셜은 null, 일반은 암호화됨
                .emailId(dto.getEmailId())
                .emailDomain(dto.getEmailDomain())
                .year(dto.getYear())
                .month(dto.getMonth())
                .day(dto.getDay())
                .gender(genderValue)
                .phone_num(dto.getPhone_num())
                .created_at(dto.getCreated_at())
                .updated_at(dto.getUpdated_at())
                .provider(dto.getProvider())
                .providerId(dto.getProviderId())
                .profileImage(dto.getProfileImage())
                .oauthEmail(dto.getOauthEmail())
                .role(Role.USER)                // ★ ROLE_USER가 아니라 USER만 저장
                .build();

        Signup saved = repository.save(entity);

        log.info("[회원가입 완료] userSeq={}, role={}", saved.getUserSeq(), saved.getRole());

        /* ------------------------------------------------------
         *  반환을 SignupDto 형태로 변환
         * ------------------------------------------------------ */
        return SignupDto.fromEntity(saved);
    }


    /** ======================================================
     *  비밀번호 유효성 검사
     * ====================================================== */
    private void validatePassword(String pw, String confirmPw) {
        if (pw == null || pw.trim().isEmpty()) {
            throw new IllegalArgumentException("비밀번호는 필수 입력값입니다.");
        }

        boolean valid =
                pw.length() >= 9 &&
                        pw.matches(".*[A-Z].*") &&
                        pw.matches(".*[!@#$%^&*].*");

        if (!valid) {
            throw new IllegalArgumentException(
                    "비밀번호는 대문자 1개 이상, 특수문자 1개 이상 포함, 최소 9자 이상이어야 합니다."
            );
        }

        if (!pw.equals(confirmPw)) {
            throw new IllegalArgumentException("비밀번호 확인이 일치하지 않습니다.");
        }
    }


    /** ======================================================
     *  아이디 중복 검사
     * ====================================================== */
    public boolean existsById(String id) {
        if (id == null || id.trim().isEmpty()) return false;
        return repository.findAll().stream()
                .anyMatch(u -> id.equals(u.getId()));
    }


    /** ======================================================
     *  소셜 로그인 중복 확인
     * ====================================================== */
    public Signup findByProviderAndProviderId(String provider, String providerId) {
        return repository.findAll().stream()
                .filter(u -> provider.equals(u.getProvider()) &&
                        providerId.equals(u.getProviderId()))
                .findFirst()
                .orElse(null);
    }

    private final SignupRepository signupRepository;

    /** 전체 회원 수 */
    public long countUsers() {
        return signupRepository.count();
    }

    /** 최근 가입자 5명 */
    public List<Signup> findRecentUsers() {
        return signupRepository.findTop5ByOrderByCreated_atDesc();
    }
}
