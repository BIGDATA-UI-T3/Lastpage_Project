package com.example.demo.Domain.Common.Service.Admin;

import com.example.demo.Domain.Common.Entity.Signup;
import com.example.demo.Repository.SignupRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminUserService {

    private final SignupRepository signupRepository;

    /** 전체 회원 수 */
    public long countUsers() {
        return signupRepository.count();
    }

    /** 최근 가입 회원 조회 (limit 개수만) */
    public List<Signup> findRecentUsers(int limit) {
        return signupRepository.findAll().stream()
                .sorted(Comparator.comparing(Signup::getCreated_at, Comparator.nullsLast(Comparator.naturalOrder()))
                        .reversed())
                .limit(limit)
                .toList();
    }

    /** 전체 회원 조회 (관리자 리스트 페이지용) */
    public List<Signup> findAllUsers() {
        return signupRepository.findAll();
    }

    /** 특정 유저 조회 */
    public Signup findByUserSeq(String userSeq) {
        return signupRepository.findById(userSeq)
                .orElse(null);
    }

    /** 회원 삭제 */
    public boolean deleteUser(String userSeq) {
        if (!signupRepository.existsById(userSeq)) return false;
        signupRepository.deleteById(userSeq);
        return true;
    }

    /** 전체 회원 조회 */
    public List<Signup> getAllUsers() {
        return signupRepository.findAll();
    }
}
