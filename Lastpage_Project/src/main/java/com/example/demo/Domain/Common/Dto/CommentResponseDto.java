package com.example.demo.Domain.Common.Dto;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentResponseDto {

    private Long id;
    private String content;

    private String writerName;
    private String writerUsername;

    // 2025/11/20 11:15 형식 문자열
    private String createdAt;

    // 내가 쓴 댓글인지 여부 (삭제 버튼 표시용)
    private boolean mine;
}
