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

    // application.properties에서 설정한 루트 경로 가져오기
    // ${user.home}/lastpage_uploads/
    @Value("${file.upload.root}")
    private String rootPath;

    // 하위 폴더 이름들 (폴더 생성용)
    @Value("${file.upload.post}")
    private String postPath;

    @Value("${file.upload.ourpage}")
    private String ourpagePath;

    @Value("${file.upload.qna}")
    private String qnaPath;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {

        // =========================================================================
        // 1. [추가된 부분] JS, CSS 등 기본 정적 리소스 경로 매핑
        // WebMvcConfigurer 구현 시 자동 설정이 비활성화되는 것을 방지합니다.
        // =========================================================================

        // 브라우저 요청: /css/**, /js/**
        // 실제 경로: classpath:/static/css/, classpath:/static/js/
        registry.addResourceHandler("/css/**", "/js/**", "/Asset/**")
                .addResourceLocations("classpath:/static/css/", "classpath:/static/js/", "classpath:/static/Asset/");

        log.info("▶ WebConfig : 기본 정적 리소스(CSS/JS) 경로 매핑 완료");

        // =========================================================================
        // 2. [기존 부분] 파일 시스템 기반의 업로드 파일 경로 매핑
        // 브라우저 요청: /uploads/**
        // 실제 연결 경로: file:///사용자홈/lastpage_uploads/
        // =========================================================================

        String resourcePath = Paths.get(rootPath).toUri().toString();

        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(resourcePath);

        log.info("▶ WebConfig : 업로드 파일 경로 매핑 완료");
        log.info("요청 URL 패턴: /uploads/**");
        log.info("실제 연결 경로: {}", resourcePath);
    }

    @PostConstruct
    public void createDirs() {
        // 서버 시작 시 폴더가 없으면 자동으로 만들어줍니다.
        create(rootPath + postPath);
        create(rootPath + ourpagePath);
        create(rootPath + qnaPath);
    }

    private void create(String path) {
        File dir = new File(path);
        if (!dir.exists()) {
            boolean ok = dir.mkdirs();
            log.info("폴더 생성 체크: {} -> {}", path, ok ? "생성됨" : "이미 존재하거나 실패");
        }
    }
}