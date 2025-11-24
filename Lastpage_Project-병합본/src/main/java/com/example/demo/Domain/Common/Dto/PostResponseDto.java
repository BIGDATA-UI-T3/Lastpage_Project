package com.example.demo.Domain.Common.Dto;

import com.example.demo.Domain.Common.Entity.Post;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PostResponseDto {
    private Long id;
    private String content;
    private String imageUrl;
    private String createdAt;
    private WriterDto writer;

    private int likeCount;
    private boolean liked;  // 내가 좋아요 눌렀는지 여부

    private String category;

    private long commentCount;

    // Post 엔티티 → DTO 변환 생성자 추가
    public PostResponseDto(Post post, boolean liked, boolean isFollowing, long commentCount) {

        this.id = post.getId();
        this.content = post.getContent();
        this.imageUrl = post.getImageUrl();
        this.createdAt = post.getCreatedAt().toString();

        // WriterDto 생성
        this.writer = WriterDto.builder()
                .userSeq(post.getWriter().getUserSeq())
                .name(post.getWriter().getName())
                .username(post.getWriter().getUsername())
                .isFollowing(isFollowing)
                .build();

        this.likeCount = post.getLikeCount();
        this.liked = liked;
        this.category = post.getCategory().name();

        this.commentCount = commentCount;
    }

}


