package com.example.demo.Controller;

import com.example.demo.Domain.Common.Dto.ProfileResponseDto;
import com.example.demo.Domain.Common.Entity.Member;
import com.example.demo.Domain.Common.Security.CustomUserPrincipal;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class CommunityController {

    // 커뮤니티 페이지 이동
    @GetMapping("/community")
    public String communityPage() {
        return "community"; // community.html
    }

    @GetMapping("/api/user/profile")
    @ResponseBody
    public ProfileResponseDto getUserProfile(@AuthenticationPrincipal CustomUserPrincipal user) {

        ProfileResponseDto dto = new ProfileResponseDto();

        // 로그인 안 되어있으면
        if (user == null) {
            dto.setLogin(false);
            return dto;
        }

        // 로그인 되어있으면
        Member member = user.getMember();

        dto.setLogin(true);
        dto.setName(member.getName());
        dto.setUsername(member.getUsername());
        dto.setBio(member.getBio());            // bio 없으면 Member 엔티티에 추가해야함
        dto.setAvatar(member.getAvatarUrl());   // avatarUrl 없으면 추가

        return dto;
    }

    @GetMapping("/comm_post")
    public String writePage() {
        return "comm_post"; // comm_post.html
    }


}
