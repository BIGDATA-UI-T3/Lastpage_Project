package com.example.demo.Domain.Common.Service;

import com.example.demo.Domain.Common.Entity.Follow;
import com.example.demo.Repository.FollowRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class FollowService {

    private final FollowRepository followRepository;

    /** 팔로우 */
    public void follow(String userUsername, String targetUsername) {

        // 1) 자기 자신 팔로우 방지
        if (userUsername == null || targetUsername == null) return;
        if (userUsername.equals(targetUsername)) return;

        // 2) 이미 팔로우하고 있는지 검사
        boolean exists = followRepository
                .existsByUserUsernameAndTargetUsername(userUsername, targetUsername);

        if (exists) {
            // 이미 팔로우 중이면 아무 동작 안함
            return;
        }

        // 3) 새롭게 팔로우 저장
        followRepository.save(new Follow(userUsername, targetUsername));
    }

    /** 언팔로우 */
    public void unfollow(String userUsername, String targetUsername) {

        if (userUsername == null || targetUsername == null) return;

        followRepository.deleteByUserUsernameAndTargetUsername(userUsername, targetUsername);
    }
}
