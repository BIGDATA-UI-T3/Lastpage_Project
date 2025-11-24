package com.example.demo.Controller;

import com.example.demo.Domain.Common.Dto.PostResponseDto;
import com.example.demo.Domain.Common.Dto.SignupDto;
import com.example.demo.Domain.Common.Entity.Signup;
import com.example.demo.Domain.Common.Service.PostService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/community")
public class CommunityPostController {

    private final PostService postService;

    /** 게시글 목록 (최신순) */
    @GetMapping("/list")
    public ResponseEntity<?> list(
            @RequestParam(required = false, defaultValue = "ALL") String category,
            HttpSession session
    ) {
        Object sessionUser = session.getAttribute("loginUser");
        String myUsername = null;

        // ===========================
        // 1) 소셜 로그인 (SignupDto)
        // ===========================
        if (sessionUser instanceof SignupDto u) {

            // DB username 우선
            if (u.getUsername() != null && !u.getUsername().isEmpty()) {
                myUsername = u.getUsername();
            }
            // 일반 로그인 id
            else if (u.getId() != null && !u.getId().isEmpty()) {
                myUsername = u.getId();
            }
            // provider + providerId
            else if (u.getProvider() != null && u.getProviderId() != null) {
                myUsername = u.getProvider() + "_" + u.getProviderId();
            }
            // oauthEmail 앞부분
            else if (u.getOauthEmail() != null) {
                myUsername = u.getOauthEmail().split("@")[0];
            } else {
                myUsername = "unknownUser";
            }
        }

        // ===========================
        // 2) 자체 로그인 (Signup 엔티티)
        // ===========================
        else if (sessionUser instanceof Signup user) {

            // DB username 우선
            if (user.getUsername() != null && !user.getUsername().isBlank()) {
                myUsername = user.getUsername();
            }
            // 자체 회원가입 id
            else if (user.getId() != null && !user.getId().isBlank()) {
                myUsername = user.getId();
            }
            // 소셜(provider + providerId) 로 들어온 경우
            else if (user.getProvider() != null && user.getProviderId() != null) {
                myUsername = user.getProvider() + "_" + user.getProviderId();
            }
            // oauthEmail 앞부분
            else if (user.getOauthEmail() != null && !user.getOauthEmail().isBlank()) {
                myUsername = user.getOauthEmail().split("@")[0];
            } else {
                myUsername = "unknownUser";
            }
        }

        // myUsername이 null이어도 서비스 쪽에서 알아서 처리하긴 하지만,
        // 지금은 로그인 상태면 대부분 위에서 값이 들어가게 됨.
        List<PostResponseDto> list = postService.getPostListDto(myUsername, category);
        return ResponseEntity.ok(list);
    }

    /** 게시글 등록 (사진 필수 + 글 선택) */
    @PostMapping("/save")
    public ResponseEntity<?> savePost(
            @RequestParam String category,
            @RequestParam(required = false) String content,
            @RequestParam(required = false, value = "image") MultipartFile image,
            HttpSession session
    ) {
        try {
            // 사진 필수 체크
            if (image == null || image.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("message", "사진을 첨부해야 합니다."));
            }

            // 실제 저장
            PostResponseDto saved = postService.savePost(content, image, category, session);
            return ResponseEntity.ok(saved);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", e.getMessage()));

        } catch (IllegalStateException e) {
            return ResponseEntity.status(401)
                    .body(Map.of("message", e.getMessage()));

        } catch (IOException e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("message", "파일 업로드 실패: " + e.getMessage()));
        }
    }

    /** 좋아요 */
    @PostMapping("/like/{postId}")
    public ResponseEntity<?> like(@PathVariable Long postId, HttpSession session) {
        try {
            Map<String, Object> result = postService.toggleLike(postId, session);
            return ResponseEntity.ok(result);  // liked + likeCount 둘 다 포함
        } catch (IllegalStateException e) {
            return ResponseEntity.status(401)
                    .body(Map.of("message", "로그인이 필요합니다."));
        }
    }

    /** 내 게시글 조회 */
    @GetMapping("/my")
    public ResponseEntity<?> getMyPosts(HttpSession session) {

        Signup user = postService.getLoginUserFromSession(session);
        if (user == null) {
            return ResponseEntity.status(401).body(Map.of("message", "로그인 필요"));
        }

        List<PostResponseDto> posts = postService.getMyPostListDto(user);
        return ResponseEntity.ok(posts);
    }

    /** 상세 보기 */
    @GetMapping("/detail/{id}")
    public ResponseEntity<?> detail(@PathVariable Long id) {
        return postService.getPost(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /** 게시글 수정 */
    @PutMapping("/update/{id}")
    public ResponseEntity<?> updatePost(
            @PathVariable Long id,
            @RequestParam(required = false) String content,
            @RequestParam(required = false) MultipartFile image,
            @RequestParam(required = false) String category,
            HttpSession session
    ) {
        try {
            PostResponseDto updated = postService.updatePost(id, content, image, category, session);
            return ResponseEntity.ok(updated);

        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", e.getMessage()));

        } catch (IOException e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("message", "파일 업로드 실패: " + e.getMessage()));
        }
    }

    /** 게시글 삭제 */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id, HttpSession session) {
        try {
            postService.deletePost(id, session);
            return ResponseEntity.ok("삭제 완료");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
