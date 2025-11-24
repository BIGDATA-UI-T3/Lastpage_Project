package com.example.demo.Domain.Common.Service;

import com.example.demo.Domain.Common.Dto.SignupDto;
import com.example.demo.Domain.Common.Entity.Gender;
import com.example.demo.Domain.Common.Entity.Signup;
import com.example.demo.Repository.SignupRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class EditInfoService {

    private final SignupRepository signupRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    /** -----------------------------------
     * 회원정보 불러오기
     * ----------------------------------- */
    @Transactional(readOnly = true)
    public SignupDto getUserInfo(String userSeq) {
        Signup user = signupRepository.findById(userSeq)
                .orElseThrow(() -> new IllegalArgumentException("해당 회원을 찾을 수 없습니다."));

        SignupDto dto = new SignupDto();
        dto.setUserSeq(user.getUserSeq());
        dto.setName(user.getName());
        dto.setEmailId(user.getEmailId());
        dto.setEmailDomain(user.getEmailDomain());
        dto.setOauthEmail(user.getOauthEmail());
        dto.setPhone_num(user.getPhone_num());
        dto.setGender(user.getGender() != null ? user.getGender().name() : null);
        dto.setYear(user.getYear());
        dto.setMonth(user.getMonth());
        dto.setDay(user.getDay());

        return dto;
    }

    /** -----------------------------------
     * 회원정보 수정 (native / social / admin)
     * ----------------------------------- */
    @Transactional
    public Signup updateUserInfo(String userSeq, SignupDto dto, String userType) {

        Signup user = signupRepository.findById(userSeq)
                .orElseThrow(() -> new IllegalArgumentException("회원 정보를 찾을 수 없습니다."));

        /* ==========================
           1) 관리자(admin) 모드
        ========================== */
        if ("admin".equals(userType)) {

            // 관리자: 모든 필드 수정 허용 (except password)
            if (dto.getName() != null && !dto.getName().isEmpty()) user.setName(dto.getName());

            if (dto.getEmailId() != null && !dto.getEmailId().isEmpty())
                user.setEmailId(dto.getEmailId());

            if (dto.getEmailDomain() != null && !dto.getEmailDomain().isEmpty())
                user.setEmailDomain(dto.getEmailDomain());

            if (dto.getPhone_num() != null && !dto.getPhone_num().isEmpty())
                user.setPhone_num(dto.getPhone_num());

            if (dto.getYear() != null) user.setYear(dto.getYear());
            if (dto.getMonth() != null) user.setMonth(dto.getMonth());
            if (dto.getDay() != null) user.setDay(dto.getDay());

            if (dto.getGender() != null) {
                try {
                    user.setGender(Gender.valueOf(dto.getGender()));
                } catch (IllegalArgumentException ignored) {
                    log.warn("잘못된 성별 값 입력: {}", dto.getGender());
                }
            }

            // 관리자 모드에서는 비밀번호 절대 변경 X
            log.info("[ADMIN] 회원정보 수정 완료 → userSeq={}", userSeq);

        }

        /* ==========================
           2) 자체 로그인(native)
        ========================== */
        else if ("native".equals(userType)) {

            if (dto.getName() != null && !dto.getName().isEmpty()) user.setName(dto.getName());
            if (dto.getPhone_num() != null && !dto.getPhone_num().isEmpty()) user.setPhone_num(dto.getPhone_num());
            if (dto.getYear() != null) user.setYear(dto.getYear());
            if (dto.getMonth() != null) user.setMonth(dto.getMonth());
            if (dto.getDay() != null) user.setDay(dto.getDay());

            if (dto.getGender() != null) {
                try {
                    user.setGender(Gender.valueOf(dto.getGender()));
                } catch (IllegalArgumentException ignored) {
                    log.warn("잘못된 성별 값 입력: {}", dto.getGender());
                }
            }

            // 이메일 변경 가능
            if (dto.getEmailId() != null && !dto.getEmailId().isEmpty())
                user.setEmailId(dto.getEmailId());

            if (dto.getEmailDomain() != null && !dto.getEmailDomain().isEmpty())
                user.setEmailDomain(dto.getEmailDomain());

            // 비밀번호 변경 처리
            if (dto.getPassword() != null && !dto.getPassword().isEmpty()) {

                if (checkPasswordReuse(userSeq, dto.getPassword())) {
                    throw new IllegalArgumentException("이전에 사용한 비밀번호는 다시 사용할 수 없습니다.");
                }
                if (!isStrongPassword(dto.getPassword())) {
                    throw new IllegalArgumentException("비밀번호는 최소 8자 이상, 영문/숫자/특수문자를 모두 포함해야 합니다.");
                }

                user.setPassword(passwordEncoder.encode(dto.getPassword()));
            }

        }

        /* ==========================
           3) 소셜 로그인(social)
        ========================== */
        else if ("social".equals(userType)) {

            if (dto.getPhone_num() != null && !dto.getPhone_num().isEmpty()) user.setPhone_num(dto.getPhone_num());
            if (dto.getYear() != null) user.setYear(dto.getYear());
            if (dto.getMonth() != null) user.setMonth(dto.getMonth());
            if (dto.getDay() != null) user.setDay(dto.getDay());

            if (dto.getGender() != null) {
                try {
                    user.setGender(Gender.valueOf(dto.getGender()));
                } catch (IllegalArgumentException ignored) {
                    log.warn("잘못된 성별 값 입력: {}", dto.getGender());
                }
            }

            log.info("[SOCIAL] 제한된 수정만 적용 → userSeq={}", userSeq);

        } else {
            throw new IllegalArgumentException("유효하지 않은 사용자 유형입니다.");
        }

        // 수정일 갱신
        user.setUpdated_at(LocalDateTime.now());
        return user;
    }

    /** -----------------------------------
     * 비밀번호 재사용 검사
     * ----------------------------------- */
    @Transactional(readOnly = true)
    public boolean checkPasswordReuse(String userSeq, String rawPassword) {
        Signup user = signupRepository.findById(userSeq)
                .orElseThrow(() -> new IllegalArgumentException("회원 정보를 찾을 수 없습니다."));
        return passwordEncoder.matches(rawPassword, user.getPassword());
    }

    /** -----------------------------------
     * 비밀번호 강도 검사
     * ----------------------------------- */
    public boolean isStrongPassword(String password) {
        if (password == null || password.length() < 8) return false;

        boolean hasLetter = password.matches(".*[a-zA-Z].*");
        boolean hasDigit = password.matches(".*[0-9].*");
        boolean hasSpecial = password.matches(".*[!@#$%^&*(),.?\":{}|<>].*");

        return hasLetter && hasDigit && hasSpecial;
    }

    @Transactional
    public boolean verifyPassword(String userSeq, String rawPassword) {
        Signup user = signupRepository.findById(userSeq)
                .orElseThrow(() -> new IllegalArgumentException("회원 정보를 찾을 수 없습니다."));
        return passwordEncoder.matches(rawPassword, user.getPassword());
    }

    @Transactional
    public void deleteUserInfo(String userSeq) {
        Signup user = signupRepository.findById(userSeq)
                .orElseThrow(() -> new IllegalArgumentException("회원 정보를 찾을 수 없습니다."));
        signupRepository.delete(user);
        log.info("[회원탈퇴 완료] userSeq={}, name={}", userSeq, user.getName());
    }
}
