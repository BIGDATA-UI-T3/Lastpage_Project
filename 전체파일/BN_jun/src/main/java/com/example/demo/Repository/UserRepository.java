package com.example.demo.Repository;

import com.example.demo.Domain.Common.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    // [추가] 이메일로 사용자를 찾는 기능을 추가합니다.
    Optional<User> findByEmail(String email);
}