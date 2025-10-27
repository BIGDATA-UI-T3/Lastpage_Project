package com.example.demo.Domain.Common.Service;

import com.example.demo.Domain.Common.Dto.MemberDto;
import com.example.demo.Domain.Common.Dto.SigninDto;
import com.example.demo.Domain.Common.Entity.Member;
import com.example.demo.Repository.MemberRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    public void signup(MemberDto dto) {
        // 비밀번호 확인
        if (!dto.getPassword().equals(dto.getConfirmPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        // 중복 체크
        if (memberRepository.findByUsername(dto.getUsername()).isPresent()) {
            throw new IllegalArgumentException("이미 사용 중인 아이디입니다.");
        }
        if (memberRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }

        // 인증번호 확인(예시)
        // 코드 살려두니까 로그인으로 넘어가질 않아서 임시 주석처리 합니다 ㅜㅜ
        // 아마 현재는 그냥 DTO만 전달하고 서버가 인증 여부를 판단하지 않으면 항상 통과하지 못해서..?
//        if (!"123456".equals(dto.getAuthCode())) { // 실제로는 Redis나 SMS 서비스로 검증
//            throw new IllegalArgumentException("인증번호가 올바르지 않습니다.");
//        }

        // 회원 저장
        Member member = Member.builder()
                .username(dto.getUsername())
                .password(passwordEncoder.encode(dto.getPassword())) // 암호화
                .name(dto.getName())
                .email(dto.getEmail())
                .birth(dto.getBirth())
                .gender(dto.getGender())
                .phone(dto.getPhone())
                .phoneVerified(true)
                .build();

        memberRepository.save(member);
    }

    public MemberDto signin(MemberDto dto) {
        Member member = memberRepository.findByUsername(dto.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("아이디 또는 비밀번호가 올바르지 않습니다."));

        // 입력 비밀번호와 DB 해시 비교
        if (!passwordEncoder.matches(dto.getPassword(), member.getPassword())) {
            throw new IllegalArgumentException("아이디 또는 비밀번호가 올바르지 않습니다.");
        }

        // 로그인 성공 시 DTO 반환
        return new MemberDto(member);
    }


}
