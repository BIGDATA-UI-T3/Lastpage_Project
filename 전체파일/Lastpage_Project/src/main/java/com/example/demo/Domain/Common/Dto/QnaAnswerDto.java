package com.example.demo.Domain.Common.Dto;

import com.example.demo.Domain.Common.Entity.Qna;
import com.example.demo.Domain.Common.Entity.Signup;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class QnaAnswerDto {

    private String qnaId;
    private String adminName;
    private String answer;

    private Qna toEntity(QnaRequestDto dto, Signup user) {

        return Qna.builder()
                .user(user)
                .nickname(dto.getNickname())
                .writerPass(dto.getWriterPass())  // 여기서는 아직 암호화 전
                .title(dto.getTitle())
                .content(dto.getContent())
                .category(dto.getCategory())
                .secret(dto.isSecret())
                .images(dto.getImages())
                .links(dto.getLinks())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }
}
