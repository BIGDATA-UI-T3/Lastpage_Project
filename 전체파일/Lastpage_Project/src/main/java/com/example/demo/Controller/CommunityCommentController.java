package com.example.demo.Controller;

import com.example.demo.Domain.Common.Dto.CommentResponseDto;
import com.example.demo.Domain.Common.Service.CommentService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/community")
public class CommunityCommentController {

    private final CommentService commentService;

    /** 댓글 목록 조회 (비로그인도 가능) */
    @GetMapping("/{postId}/comments")
    public ResponseEntity<?> getComments(@PathVariable Long postId, HttpSession session) {
        List<CommentResponseDto> list = commentService.getComments(postId, session);
        return ResponseEntity.ok(list);
    }

    /** 댓글 등록 (로그인 필요) */
    @PostMapping("/{postId}/comments")
    public ResponseEntity<?> addComment(
            @PathVariable Long postId,
            @RequestBody Map<String, String> body,
            HttpSession session
    ) {
        try {
            String content = body.get("content");
            CommentResponseDto dto = commentService.addComment(postId, content, session);
            return ResponseEntity.ok(dto);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(401)
                    .body(Map.of("message", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", e.getMessage()));
        }
    }

    /** 댓글 삭제 (본인만 삭제 가능) */
    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<?> deleteComment(
            @PathVariable Long commentId,
            HttpSession session
    ) {
        try {
            commentService.deleteComment(commentId, session);
            return ResponseEntity.ok(Map.of("message", "삭제 완료"));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(401).body(Map.of("message", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
}
