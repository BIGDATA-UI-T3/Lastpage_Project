package com.example.demo.Controller;

import com.example.demo.Domain.Common.Dto.SignupDto;
import com.example.demo.Domain.Common.Entity.Signup;
import com.example.demo.Domain.Common.Service.FollowService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/follow")
public class FollowController {

    private final FollowService followService;

    /** 세션에서 username 계산 */
    private String getLoginUsername(HttpSession session) {
        Object obj = session.getAttribute("loginUser");

        // ============================
        // 1) 자체 로그인(Signup 엔티티)
        // ============================
        if (obj instanceof Signup entity) {

            // DB username이 있으면 우선 사용
            if (entity.getUsername() != null && !entity.getUsername().isEmpty())
                return entity.getUsername();

            // 일반 로그인 id
            if (entity.getId() != null && !entity.getId().isEmpty())
                return entity.getId();

            // provider 기반 (소셜)
            if (entity.getProvider() != null && entity.getProviderId() != null)
                return entity.getProvider() + "_" + entity.getProviderId();

            // oauth 이메일
            if (entity.getOauthEmail() != null)
                return entity.getOauthEmail().split("@")[0];
        }

        // ============================
        // 2) 소셜 로그인(SignupDto)
        // ============================
        if (obj instanceof SignupDto dto) {

            if (dto.getUsername() != null && !dto.getUsername().isEmpty())
                return dto.getUsername();

            if (dto.getId() != null && !dto.getId().isEmpty())
                return dto.getId();

            if (dto.getProvider() != null && dto.getProviderId() != null)
                return dto.getProvider() + "_" + dto.getProviderId();

            if (dto.getOauthEmail() != null)
                return dto.getOauthEmail().split("@")[0];
        }

        return null;
    }


    // 팔로우
    @PostMapping("/{targetUsername}")
    public ResponseEntity<?> follow(
            @PathVariable String targetUsername,
            HttpSession session
    ) {
        String me = getLoginUsername(session);
        if (me == null)
            return ResponseEntity.status(401).body("로그인 필요");

        if (me.equals(targetUsername))
            return ResponseEntity.badRequest().body("본인 팔로우 불가");

        followService.follow(me, targetUsername);
        return ResponseEntity.ok("followed");
    }

    /**
     * 언팔로우
     */
    @DeleteMapping("/{targetUsername}")
    public ResponseEntity<?> unfollow(
            @PathVariable String targetUsername,
            HttpSession session
    ) {
        String me = getLoginUsername(session);
        if (me == null)
            return ResponseEntity.status(401).body("로그인 필요");

        followService.unfollow(me, targetUsername);
        return ResponseEntity.ok("unfollowed");
    }
}
