package com.example.demo.Config;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;
import java.nio.file.Paths;

@Slf4j
@Configuration
public class WebConfig implements WebMvcConfigurer {

    // root directory: /Users/.../lastpage_uploads/
    @Value("${file.upload.root}")
    private String rootPath;

    // sub directories: post/, ourpage/, qna/
    @Value("${file.upload.post}")
    private String postPath;

    @Value("${file.upload.ourpage}")
    private String ourpagePath;

    @Value("${file.upload.qna}")
    private String qnaPath;


    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {

        // ----------- POST 이미지 (커뮤니티) -----------
        String postFullPath = Paths.get(rootPath, postPath).toUri().toString();
        registry.addResourceHandler("/uploads/post/**")
                .addResourceLocations(postFullPath);

        // ----------- OURPAGE 이미지 -----------
        String ourpageFullPath = Paths.get(rootPath, ourpagePath).toUri().toString();
        registry.addResourceHandler("/uploads/ourpage/**")
                .addResourceLocations(ourpageFullPath);

        // ----------- QNA 이미지 -----------
        String qnaFullPath = Paths.get(rootPath, qnaPath).toUri().toString();
        registry.addResourceHandler("/uploads/qna/**")
                .addResourceLocations(qnaFullPath);

        log.info("▶ Static upload paths mapped:");
        log.info("POST    → {}", postFullPath);
        log.info("OURPAGE → {}", ourpageFullPath);
        log.info("QNA     → {}", qnaFullPath);
    }


    @PostConstruct
    public void createDirs() {
        create(rootPath + postPath);
        create(rootPath + ourpagePath);
        create(rootPath + qnaPath);
    }

    private void create(String path) {
        File dir = new File(path);
        if (!dir.exists()) {
            boolean ok = dir.mkdirs();
            log.info("Create directory {} : {}", path, ok ? "OK" : "FAIL");
        }
    }
}
//package com.example.demo.Config;
//
//import jakarta.annotation.PostConstruct;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
//import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
//
//import java.io.File;
//import java.nio.file.Paths;
//
//@Slf4j
//@Configuration
//public class WebConfig implements WebMvcConfigurer {
//
//    // application.properties에서 설정한 루트 경로 가져오기
//    // ${user.home}/lastpage_uploads/
//    @Value("${file.upload.root}")
//    private String rootPath;
//
//    // 하위 폴더 이름들 (폴더 생성용)
//    @Value("${file.upload.post}")
//    private String postPath;
//
//    @Value("${file.upload.ourpage}")
//    private String ourpagePath;
//
//    @Value("${file.upload.qna}")
//    private String qnaPath;
//
//    @Override
//    public void addResourceHandlers(ResourceHandlerRegistry registry) {
//        // [핵심 변경] 개별 매핑 대신 루트 경로 하나만 매핑합니다.
//        // 브라우저 요청: /uploads/** (예: /uploads/ourpage/abc.jpg)
//        // 실제 연결 경로: file:///사용자홈/lastpage_uploads/  (예: .../lastpage_uploads/ourpage/abc.jpg)
//
//        // Paths.get().toUri().toString()을 쓰면 OS(윈도우/맥)에 맞춰서 file:/// 경로를 잘 만들어줍니다.
//        String resourcePath = Paths.get(rootPath).toUri().toString();
//
//        registry.addResourceHandler("/uploads/**")
//                .addResourceLocations(resourcePath);
//
//        log.info("▶ WebConfig 경로 매핑 완료");
//        log.info("요청 URL 패턴: /uploads/**");
//        log.info("실제 연결 경로: {}", resourcePath);
//    }
//
//    @PostConstruct
//    public void createDirs() {
//        // 서버 시작 시 폴더가 없으면 자동으로 만들어줍니다.
//        // rootPath + "post/" 이런 식으로 조합됨
//        create(rootPath + postPath);
//        create(rootPath + ourpagePath);
//        create(rootPath + qnaPath);
//    }
//
//    private void create(String path) {
//        File dir = new File(path);
//        if (!dir.exists()) {
//            boolean ok = dir.mkdirs();
//            log.info("폴더 생성 체크: {} -> {}", path, ok ? "생성됨" : "이미 존재하거나 실패");
//        }
//    }
//}