package com.example.demo.Repository;

import com.example.demo.Domain.Common.Entity.Post;
import com.example.demo.Domain.Common.Entity.PostCategory;
import com.example.demo.Domain.Common.Entity.Signup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {
    List<Post> findByWriter(Signup writer);

    // 최신순 정렬
    @Query("SELECT p FROM Post p ORDER BY p.createdAt DESC")
    List<Post> findAllByOrderByCreatedAtDesc();

    List<Post> findByCategoryOrderByCreatedAtDesc(PostCategory category);
}

