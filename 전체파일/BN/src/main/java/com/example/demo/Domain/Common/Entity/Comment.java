package com.example.demo.Domain.Common.Entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "post_comment")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 어떤 게시글의 댓글인지
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    // 댓글 작성자
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_seq", nullable = false)
    private Signup writer;


//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "post_id", nullable = false)
//    @JsonIgnoreProperties({"comments", "writer"})
//    private Post post;
//
//    @ManyToOne(fetch = FetchType.EAGER)
//    @JoinColumn(name = "user_seq", nullable = false)
//    @JsonIgnoreProperties({"posts", "comments"})
//    private Signup writer;

    @Column(nullable = false, length = 300)
    private String content;

    private LocalDateTime createdAt;
}
