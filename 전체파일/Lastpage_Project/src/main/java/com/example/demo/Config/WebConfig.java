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
