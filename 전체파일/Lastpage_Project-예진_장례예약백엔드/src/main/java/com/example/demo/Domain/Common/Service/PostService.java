package com.example.demo.Domain.Common.Service;

import com.example.demo.Domain.Common.Entity.Post;
import com.example.demo.Domain.Common.Entity.Member;
import com.example.demo.Domain.Common.Entity.PostLike;
import com.example.demo.Repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.example.demo.Repository.PostLikeRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final String uploadDir = "C:/upload/posts";
    private final PostLikeRepository postLikeRepository;

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
                .orElseThrow(() -> new RuntimeException("게시글을 찾을 수 없습니다."));

        // 회원 + 게시글 조합으로 좋아요 기록 존재 여부 확인
        Optional<PostLike> existing = postLikeRepository.findByPostAndMember(post, member);

        if (existing.isPresent()) {
            // 이미 눌렀으면 좋아요 취소
            postLikeRepository.delete(existing.get());
            post.setLikeCount(post.getLikeCount() - 1);
        } else {
            // 처음 누름 → 좋아요 추가
            PostLike newLike = PostLike.builder()
                    .post(post)
                    .member(member)
                    .build();
            postLikeRepository.save(newLike);
            post.setLikeCount(post.getLikeCount() + 1);
        }

        // 최종 카운트 DB 반영 후 반환
        return postRepository.save(post).getLikeCount();
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
