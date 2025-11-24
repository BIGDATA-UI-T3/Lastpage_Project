package com.example.demo.Domain.Common.Service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class QnaImageService {

    // OS 관계없이 항상 동일한 경로 사용 (Mac/Windows/Linux 모두 가능)
    private final String uploadDir = System.getProperty("user.home") + "/lastpage_uploads/qna/";

    public List<String> saveBase64Images(List<String> base64List) {

        // 최종 저장 디렉토리 생성
        File dir = new File(uploadDir);
        if (!dir.exists()) dir.mkdirs();

        List<String> resultUrls = new ArrayList<>();

        for (String base64 : base64List) {
            if (base64 == null || base64.isBlank()) continue;

            try {
                String[] parts = base64.split(",");
                if (parts.length != 2) continue;

                String header = parts[0];
                String data = parts[1];

                // 확장자 판별
                String ext = ".png";
                if (header.contains("jpeg")) ext = ".jpg";
                if (header.contains("jpg"))  ext = ".jpg";

                // 파일명 생성
                String filename = UUID.randomUUID() + ext;
                File output = new File(dir, filename);

                // Base64 디코딩 후 파일 쓰기
                byte[] decoded = Base64.getDecoder().decode(data);

                try (FileOutputStream fos = new FileOutputStream(output)) {
                    fos.write(decoded);
                }

                // 웹에서 접근 가능한 URL 반환
                resultUrls.add("/uploads/qna/" + filename);

            } catch (Exception e) {
                throw new RuntimeException("이미지 저장 실패", e);
            }
        }

        return resultUrls;
    }
}
