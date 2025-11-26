package com.example.demo.Repository;

import com.example.demo.Domain.Common.Entity.Signup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SignupRepository extends JpaRepository<Signup, String> {

    /** ------------------------------
     *  자체 로그인 회원 (provider=null)
     *  → 우리 웹 회원 아이디로 로그인할 때 사용
     * ------------------------------ */
    Optional<Signup> findByIdAndProviderIsNull(String id);

    /** ------------------------------
     *  로그인 ID로 회원 조회 (provider 여부 무관)
     * ------------------------------ */
    Optional<Signup> findById(String id);

    /** ------------------------------
     *  회원가입 중복검사용
     * ------------------------------ */
    boolean existsById(String id);

    /** ------------------------------
     *  소셜 로그인 회원 조회
     * ------------------------------ */
    Optional<Signup> findByProviderAndProviderId(String provider, String providerId);

    /** ------------------------------
     *  이메일 기반 회원 조회 (이메일 아이디 + 도메인)
     * ------------------------------ */
    Optional<Signup> findByEmailIdAndEmailDomain(String emailId, String emailDomain);

    /** ------------------------------
     *  소셜 로그인 이메일로 회원 조회 (oauthEmail 기반)
     * ------------------------------ */
    Optional<Signup> findByOauthEmail(String oauthEmail);

    //  post
    Optional<Signup> findByUserSeq(String userSeq);

    //  Like
    Optional<Signup> findByUsername(String username);
    // 최근 가입자 5명
    @Query("SELECT s FROM Signup s ORDER BY s.created_at DESC")
    List<Signup> findTop5ByOrderByCreated_atDesc();

    // 전체 회원 수
    long count();

    Optional<Signup> findByNameAndEmailIdAndEmailDomainAndProviderIsNull(
            String name,
            String emailId,
            String emailDomain
    );

    Optional<Signup> findByIdAndEmailIdAndEmailDomainAndProviderIsNull(
            String id,
            String emailId,
            String emailDomain
    );

}
