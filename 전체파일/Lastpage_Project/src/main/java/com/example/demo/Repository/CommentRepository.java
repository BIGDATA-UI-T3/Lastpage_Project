package com.example.demo.Repository;

import com.example.demo.Domain.Common.Entity.Comment;
import com.example.demo.Domain.Common.Entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    List<Comment> findByPostOrderByCreatedAtAsc(Post post);

    int countByPost(Post post);
}
