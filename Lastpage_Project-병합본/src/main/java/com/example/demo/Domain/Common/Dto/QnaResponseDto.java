package com.example.demo.Domain.Common.Dto;

import com.example.demo.Domain.Common.Entity.Qna;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
public class QnaResponseDto {

    private String id;

    private String userSeq;
    private String nickname;

    private String title;
    private String content;
    private String category;

    private boolean secret;

    private List<String> images;
    private List<String> links;

    /* 관리자 답변 */
    private String adminAnswer; // null → "" 로 통일해주는 게 포인트
    private String adminName;
    private LocalDateTime answerAt;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;


    /* =====================================================
        1) Entity → DTO (단건 변환)
       ===================================================== */
    public static QnaResponseDto fromEntity(Qna q) {

        return QnaResponseDto.builder()
                .id(q.getId())
                .userSeq(q.getUser() != null ? q.getUser().getUserSeq() : null)
                .nickname(q.getNickname())
                .title(q.getTitle())
                .content(q.getContent())
                .category(q.getCategory())
                .secret(q.isSecret())
                .images(q.getImages() != null ? q.getImages() : List.of())
                .links(q.getLinks() != null ? q.getLinks() : List.of())

                // 관리자 답변 null-safe 처리
                .adminAnswer(q.getAdminAnswer() != null ? q.getAdminAnswer() : "")
                .adminName(q.getAdminName())
                .answerAt(q.getAnswerAt())

                .createdAt(q.getCreatedAt())
                .updatedAt(q.getUpdatedAt())
                .build();
    }


    /* =====================================================
        2) Entity List → DTO List 변환
       ===================================================== */
    public static List<QnaResponseDto> fromEntities(List<Qna> list) {
        return list.stream()
                .map(QnaResponseDto::fromEntity)
                .collect(Collectors.toList());
    }
}
