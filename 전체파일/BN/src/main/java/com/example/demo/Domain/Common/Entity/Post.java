package com.example.demo.Domain.Common.Entity;

import com.example.demo.Domain.Common.Entity.Comment;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "community_post")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 게시글 고유번호

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content; // 내용

    private String imageUrl;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private int likeCount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_seq") // Signup의 userSeq를 외래키로 사용
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Signup writer;

//    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
//    private List<Comment> comments;
@OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
@JsonIgnore
private List<Comment> comments;


    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PostCategory category;

}
