package com.example.demo.Controller;

import com.example.demo.Domain.Community.Entity.Post;
import com.example.demo.Domain.Common.Entity.Comment;
import com.example.demo.Domain.Common.Security.CustomUserPrincipal;
import com.example.demo.Repository.CommentRepository;
import com.example.demo.Repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/comm/comment")
@RequiredArgsConstructor
public class CommentController {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;

    @PostMapping("/{postId}")
    public ResponseEntity<Comment> addComment(
            @PathVariable Long postId,
            @RequestParam("content") String content,
            @AuthenticationPrincipal CustomUserPrincipal user) {
        if (user == null) return ResponseEntity.status(401).build();
        Post post = postRepository.findById(postId).orElseThrow();
        Comment c = Comment.builder()
                .post(post)
                .member(user.getMember())
                .content(content)
                .createdAt(LocalDateTime.now())
                .build();
        return ResponseEntity.ok(commentRepository.save(c));
    }

    @GetMapping("/{postId}")
    public ResponseEntity<List<Comment>> getComments(@PathVariable Long postId) {
        return ResponseEntity.ok(commentRepository.findByPostIdOrderByCreatedAtAsc(postId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteComment(@PathVariable Long id,
                                              @AuthenticationPrincipal CustomUserPrincipal user) {
        if (user == null) return ResponseEntity.status(401).build();

        Comment c = commentRepository.findById(id).orElseThrow();
        if (!c.getMember().getId().equals(user.getMember().getId()))
            return ResponseEntity.status(403).build();

        commentRepository.delete(c);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<Comment> editComment(@PathVariable Long id,
                                               @RequestParam("content") String content,
                                               @AuthenticationPrincipal CustomUserPrincipal user) {
        if (user == null) return ResponseEntity.status(401).build();

        Comment c = commentRepository.findById(id).orElseThrow();
        if (!c.getMember().getId().equals(user.getMember().getId()))
            return ResponseEntity.status(403).build();

        c.setContent(content);
        return ResponseEntity.ok(commentRepository.save(c));
    }

}
