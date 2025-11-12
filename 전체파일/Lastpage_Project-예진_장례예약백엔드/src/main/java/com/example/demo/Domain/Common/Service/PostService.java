package com.example.demo.Domain.Common.Service;

import com.example.demo.Domain.Common.Entity.Post;
import com.example.demo.Domain.Common.Entity.Member;
import com.example.demo.Repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final String uploadDir = "C:/upload/posts";

    public Post savePost(Member member, String content, MultipartFile imageFile) throws IOException {
        String imageUrl = null;

        if (imageFile != null && !imageFile.isEmpty()) {
            String fileName = UUID.randomUUID() + "_" + imageFile.getOriginalFilename();
            File dest = new File(uploadDir, fileName);
            imageFile.transferTo(dest);
            imageUrl = "/uploads/posts/" + fileName;
        }

        Post post = Post.builder()
                .content(content)
                .imageUrl(imageUrl)
                .member(member)
                .createdAt(LocalDateTime.now())
                .build();

        return postRepository.save(post);
    }

    public List<Post> getAllPosts() {
        return postRepository.findAllByOrderByCreatedAtDesc();
    }

    @Transactional
    public int toggleLike(Long postId, Member member) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("게시글 없음"));
        // 단순 likeCount 증가 (토글 없이)
        post.setLikeCount(post.getLikeCount() + 1);
        return postRepository.save(post).getLikeCount();
    }

    public List<Post> getMyPosts(Member member) {
        return postRepository.findAllByMemberOrderByCreatedAtDesc(member);
    }

    @Transactional
    public void deleteMyPost(Long id, Member member) {
        Post post = postRepository.findById(id).orElseThrow();
        if (!post.getMember().getId().equals(member.getId())) {
            throw new RuntimeException("삭제 권한이 없습니다.");
        }
        postRepository.delete(post);
    }



}
