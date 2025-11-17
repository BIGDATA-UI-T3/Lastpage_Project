package com.example.demo.Domain.Common.Dto;

import lombok.Data;
import java.util.List;

@Data
public class QnaRequestDto {

    private String id;          // QnA ID (수정 시 필수)
    private String userSeq;     // 작성자 user_seq (유저 작성 시 필요)
    private String nickname;    // 작성자 닉네임
    private String writerPass;  // 비밀번호 (수정/삭제 시 입력해야 함)

    private String title;
    private String content;
    private String category;
    private boolean secret;

    private List<String> images;   // 이미지 URL 0~3개
    private List<String> links;    // 링크 목록
}
