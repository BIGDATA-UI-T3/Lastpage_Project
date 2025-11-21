package com.example.demo.Repository;

import com.example.demo.Domain.Common.Entity.Follow;
import com.example.demo.Domain.Common.Entity.Signup;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FollowRepository extends JpaRepository<Follow, Long> {

    boolean existsByUserUsernameAndTargetUsername(String userUsername, String targetUsername);

    int countByUserUsername(String userUsername);

    int countByTargetUsername(String targetUsername);

    void deleteByUserUsernameAndTargetUsername(String userUsername, String targetUsername);
}

