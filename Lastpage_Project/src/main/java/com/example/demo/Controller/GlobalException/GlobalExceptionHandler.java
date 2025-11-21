package com.example.demo.Controller.GlobalException;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.util.Map;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    @ExceptionHandler(Exception.class)
    public String allException_Handler(Exception e){

        log.info("Global Exception Handler..."+e);
        return "global_error";
    }

    /** 파일 업로드 크기 초과 */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<?> handleMaxUploadSize(MaxUploadSizeExceededException e) {
        return ResponseEntity.badRequest()
                .body(Map.of("message", "업로드 가능한 파일 크기를 초과했습니다. (최대 50MB)"));
    }
}
