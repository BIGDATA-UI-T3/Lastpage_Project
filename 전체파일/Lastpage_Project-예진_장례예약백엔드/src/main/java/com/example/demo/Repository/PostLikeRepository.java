package com.example.demo.Repository;

import com.example.demo.Domain.Common.Entity.Post;
import com.example.demo.Domain.Common.Entity.PostLike;
import com.example.demo.Domain.Common.Entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface PostLikeRepository extends JpaRepository<PostLike, Long> {
    Optional<PostLike> findByPostAndMember(Post post, Member member);
    long countByPost(Post post);
}
