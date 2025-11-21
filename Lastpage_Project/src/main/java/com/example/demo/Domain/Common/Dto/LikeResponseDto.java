package com.example.demo.Domain.Common.Dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LikeResponseDto {
    private int likeCount;   // 좋아요 개수
    private boolean liked;   // 좋아요 눌렀는지 여부
}
