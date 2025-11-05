package com.example.demo.Domain.Common.Service;

import com.example.demo.Domain.Common.Dto.SignupDto;
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
     * 회원정보 불러오기 (마이페이지용)
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
        dto.setOauthEmail(user.getOauthEmail()); // 소셜 로그인용
        dto.setPhone_num(user.getPhone_num());
        dto.setGender(user.getGender());
        dto.setYear(user.getYear());
        dto.setMonth(user.getMonth());
        dto.setDay(user.getDay());

        return dto;
    }

    /** -----------------------------------
     * 회원정보 수정 (자체 / 소셜 구분)
     *  → 영속성 기반 변경감지 적용
     * ----------------------------------- */
    @Transactional
    public Signup updateUserInfo(String userSeq, SignupDto dto, String userType) {
        Signup user = signupRepository.findById(userSeq)
                .orElseThrow(() -> new IllegalArgumentException("회원 정보를 찾을 수 없습니다."));

        // 자체 로그인 사용자 (모든 항목 수정 가능)
        if ("native".equals(userType)) {

            if (dto.getName() != null && !dto.getName().isEmpty()) user.setName(dto.getName());
            if (dto.getPhone_num() != null && !dto.getPhone_num().isEmpty()) user.setPhone_num(dto.getPhone_num());
            if (dto.getYear() != null) user.setYear(dto.getYear());
            if (dto.getMonth() != null) user.setMonth(dto.getMonth());
            if (dto.getDay() != null) user.setDay(dto.getDay());

            if (dto.getGender() != null) {
                try {
                    user.setGender(dto.getGender());
                } catch (IllegalArgumentException ignored) {
                    log.warn("잘못된 성별 값 입력: {}", dto.getGender());
                }
            }

            // 이메일 수정 허용 (native만)
            if (dto.getEmailId() != null && !dto.getEmailId().isEmpty()) {
                user.setEmailId(dto.getEmailId());
            }
            if (dto.getEmailDomain() != null && !dto.getEmailDomain().isEmpty()) {
                user.setEmailDomain(dto.getEmailDomain());
            }

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

            // 소셜 로그인 사용자 (일부만 수정 가능)
        } else if ("social".equals(userType)) {

            if (dto.getPhone_num() != null && !dto.getPhone_num().isEmpty()) user.setPhone_num(dto.getPhone_num());
            if (dto.getYear() != null) user.setYear(dto.getYear());
            if (dto.getMonth() != null) user.setMonth(dto.getMonth());
            if (dto.getDay() != null) user.setDay(dto.getDay());
            if (dto.getGender() != null) {
                try {
                    user.setGender(dto.getGender());
                } catch (IllegalArgumentException ignored) {
                    log.warn("잘못된 성별 값 입력: {}", dto.getGender());
                }
            }

            // 소셜회원은 이름, 이메일, 비밀번호 수정 불가
            log.info("소셜 로그인 회원 수정 항목 제한 적용됨 (userSeq={})", userSeq);

        } else {
            throw new IllegalArgumentException("유효하지 않은 사용자 유형입니다.");
        }

        // 변경감지에 의해 자동 업데이트됨 (save 불필요)
        user.setUpdated_at(LocalDateTime.now());

        log.info("[회원정보 수정 완료] userSeq={}, userType={}", userSeq, userType);
        return user; // 컨트롤러에서 세션 갱신용으로 반환
    }

    /** -----------------------------------
     * 비밀번호 재사용 검사 (true = 재사용됨)
     * ----------------------------------- */
    @Transactional(readOnly = true)
    public boolean checkPasswordReuse(String userSeq, String rawPassword) {
        Signup user = signupRepository.findById(userSeq)
                .orElseThrow(() -> new IllegalArgumentException("회원 정보를 찾을 수 없습니다."));
        String encoded = user.getPassword();
        return passwordEncoder.matches(rawPassword, encoded);
    }

    /** -----------------------------------
     * 비밀번호 강도 검사 (안전도 체크)
     * ----------------------------------- */
    public boolean isStrongPassword(String password) {
        if (password == null || password.length() < 8) return false;

        boolean hasLetter = password.matches(".*[a-zA-Z].*");
        boolean hasDigit = password.matches(".*[0-9].*");
        boolean hasSpecial = password.matches(".*[!@#$%^&*(),.?\":{}|<>].*");

        return hasLetter && hasDigit && hasSpecial;
    }
}
