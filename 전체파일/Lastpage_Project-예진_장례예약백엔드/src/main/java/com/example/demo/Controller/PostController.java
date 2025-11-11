package com.example.demo.Controller;

import com.example.demo.Domain.Common.Entity.Post;
import com.example.demo.Domain.Common.Security.CustomUserPrincipal;
import com.example.demo.Domain.Common.Service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/comm")
public class PostController {

    private final PostService postService;

    @PostMapping("/save")
    public ResponseEntity<Post> savePost(
            @RequestParam("content") String content,
            @RequestParam(value = "image", required = false) MultipartFile imageFile,
            @AuthenticationPrincipal CustomUserPrincipal user) throws IOException {

        if (user == null) {
            return ResponseEntity.status(401).build();
        }

        Post saved = postService.savePost(user.getMember(), content, imageFile);
        return ResponseEntity.ok(saved);
    }

    @GetMapping("/list")
    public ResponseEntity<List<Post>> getAllPosts() {
        return ResponseEntity.ok(postService.getAllPosts());
    }

    @PostMapping("/like/{id}")
    public ResponseEntity<Map<String, Object>> toggleLike(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserPrincipal user) {
        Map<String, Object> result = new HashMap<>();
        if (user == null) {
            result.put("status", "fail");
            return ResponseEntity.status(401).body(result);
        }
        int newCount = postService.toggleLike(id, user.getMember());
        result.put("status", "success");
        result.put("likeCount", newCount);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/my")
    public ResponseEntity<List<Post>> getMyPosts(@AuthenticationPrincipal CustomUserPrincipal user) {
        if (user == null) return ResponseEntity.status(401).build();
        return ResponseEntity.ok(postService.getMyPosts(user.getMember()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePost(@PathVariable Long id,
                                           @AuthenticationPrincipal CustomUserPrincipal user) {
        if (user == null) return ResponseEntity.status(401).build();
        postService.deleteMyPost(id, user.getMember());
        return ResponseEntity.ok().build();
    }


}
