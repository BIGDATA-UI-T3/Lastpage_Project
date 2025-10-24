package com.example.demo.Domain.Common.Dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterFormDto {

    @NotBlank(message = "이름은 필수 입력 항목입니다.")
    @Size(min = 2, message = "이름은 2자 이상이어야 합니다.")
    private String name;

    // ▼▼▼ [수정] 아이디: 4자 -> 8자 이상 ▼▼▼
    @NotBlank(message = "아이디는 필수 입력 항목입니다.")
    @Size(min = 8, max = 20, message = "아이디는 8자 이상 20자 이하로 입력해주세요.")
    private String username;

    // ▼▼▼ [수정] 비밀번호: "가장 대중적인" (영문+숫자, 8~16자) 규칙으로 변경 ▼▼▼
    @NotBlank(message = "비밀번호는 필수 입력 항목입니다.")
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,16}$",
            message = "비밀번호는 8~16자, 영문과 숫자를 최소 1개씩 포함해야 합니다.")
    private String password;
    // (기존 특수문자 필수 규칙보다 훨씬 대중적입니다)

    @NotBlank(message = "비밀번호 확인은 필수입니다.")
    private String passwordCheck;

    @NotBlank(message = "이메일은 필수입니다.")
    private String emailId;

    @NotBlank(message = "이메일 도메인은 필수입니다.")
    private String emailDomain;

    @NotBlank(message = "생년은 필수입니다.")
    private String birthYear;
    @NotBlank(message = "월은 필수입니다.")
    private String birthMonth;
    @NotBlank(message = "일은 필수입니다.")
    private String birthDay;

    private String gender;

    @NotBlank(message = "휴대폰 번호는 필수 입력 항목입니다.")
    @Pattern(regexp = "^01(?:0|1|[6-9])(?:\\d{3}|\\d{4})\\d{4}",
            message = "휴대폰 번호 형식이 올바르지 않습니다. (예: 01012345678)")
    private String phone;

    @NotBlank(message = "이메일 인증 코드를 입력해주세요.")
    private String smsCode; // (필드 이름은 smsCode 그대로 사용)
}