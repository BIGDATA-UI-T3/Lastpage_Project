package com.example.demo.Repository;

import com.example.demo.Domain.Common.Entity.Post;
import com.example.demo.Domain.Common.Entity.PostLike;
import com.example.demo.Domain.Common.Entity.Signup;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostLikeRepository extends JpaRepository<PostLike, Long> {

    boolean existsByPostAndUser(Post post, Signup user);

    void deleteByPostAndUser(Post post, Signup user);

    int countByPost(Post post);

    boolean existsByPostIdAndUserUsername(Long id, String username);
}

