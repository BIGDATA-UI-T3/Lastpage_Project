package com.example.demo.Domain.Common.Service;

import com.example.demo.Domain.Common.Dto.MemberDto;
import com.example.demo.Domain.Common.Entity.Member;
import com.example.demo.Repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CommunityUserService {

    private final MemberRepository memberRepository;

    public MemberDto getUserProfile(String username) {

        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 기존 Member -? DTO 로 변환하는 생성자 사용
        return new MemberDto(member);
    }
}
