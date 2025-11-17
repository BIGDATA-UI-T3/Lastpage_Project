package com.example.demo.Controller;

import com.example.demo.Domain.Common.Service.QnaImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/supportService/api/upload")
public class QnaImageController {

    private final QnaImageService qnaImageService;

    /**
     * 이미지 파일 업로드 (MultipartFile)
     * 실제로 Base64 방식이면 필요 없음!
     */
    @PostMapping("/multipart")
    public List<String> uploadMultipart(@RequestParam("files") List<MultipartFile> files) {

        // MultipartFile → Base64 변환이 필요하면 구현 가능
        throw new UnsupportedOperationException("Base64 업로드 방식에서는 사용되지 않습니다.");
    }
}
