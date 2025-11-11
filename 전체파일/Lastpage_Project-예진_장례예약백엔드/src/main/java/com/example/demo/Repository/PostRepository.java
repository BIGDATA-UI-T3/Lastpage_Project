package com.example.demo.Repository;

import com.example.demo.Domain.Common.Entity.Member;
import com.example.demo.Domain.Common.Entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {
    List<Post> findAllByOrderByCreatedAtDesc();
    List<Post> findAllByMemberOrderByCreatedAtDesc(Member member);
}
