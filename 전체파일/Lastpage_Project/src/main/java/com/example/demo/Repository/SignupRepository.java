package com.example.demo.Repository;


import com.example.demo.Domain.Common.Entity.Signup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SignupRepository extends JpaRepository<Signup,String> {
    Optional<Signup> findById(String id);
    boolean existsById(String id);
    Optional<Signup> findByProviderAndProviderId(String provider, String providerId);
    Optional<Signup> findByEmailIdAndEmailDomain(String email_id, String email_domain);
}
