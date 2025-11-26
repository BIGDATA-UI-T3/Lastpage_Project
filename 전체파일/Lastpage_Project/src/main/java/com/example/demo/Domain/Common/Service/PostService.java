package com.example.demo.Domain.Common.Service;

import com.example.demo.Domain.Common.Dto.PostResponseDto;
import com.example.demo.Domain.Common.Dto.SignupDto;
import com.example.demo.Domain.Common.Dto.WriterDto;
import com.example.demo.Domain.Common.Entity.Post;
import com.example.demo.Domain.Common.Entity.PostCategory;
import com.example.demo.Domain.Common.Entity.PostLike;
import com.example.demo.Domain.Common.Entity.Signup;
import com.example.demo.Repository.*;
import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value; // ✅ 추가
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostService {
    private final PostRepository postRepository;
    private final SignupRepository signupRepository;
    private final FollowRepository followRepository;
    private final PostLikeRepository postLikeRepository;
    private final CommentRepository commentRepository;

    // application.properties 값 주입 (WebConfig와 동일하게 경로 관리)
    @Value("${file.upload.root}")
    private String rootPath;

    @Value("${file.upload.post}")
    private String postPath;


    /** 전체 게시글 (최신순 정렬) */
    public List<Post> getAllPosts() {
        List<Post> list = postRepository.findAll();
        list.sort(Comparator.comparing(Post::getCreatedAt).reversed());
        return list;
    }

    /** 게시글 등록 (사진 필수, 글 선택) */
    public PostResponseDto savePost(String content, MultipartFile image, String category, HttpSession session) throws IOException {

        // -----------------------------
        // 1. 세션 사용자 가져오기 (Signup / SignupDto 둘 다 지원)
        // -----------------------------
        Object sessionUser = session.getAttribute("loginUser");
        Signup loginUser;

        if (sessionUser instanceof Signup entityUser) {
            loginUser = entityUser;

        } else if (sessionUser instanceof SignupDto dtoUser) {
            loginUser = signupRepository.findByUserSeq(dtoUser.getUserSeq())
                    .orElseThrow(() -> new IllegalStateException("사용자 정보를 찾을 수 없습니다."));

        } else {
            throw new IllegalStateException("로그인이 필요합니다.");
        }

        // -----------------------------
        // 2. 이미지 필수 체크
        // -----------------------------
        if (image == null || image.isEmpty()) {
            throw new IllegalArgumentException("사진을 첨부해야 합니다.");
        }

        // -----------------------------
        // 3. 이미지 저장 (통일된 구조: /lastpage_uploads/post/ )
        // -----------------------------
        //  수정: 하드코딩된 경로 대신 주입받은 rootPath + postPath 사용
        String uploadDir = rootPath + postPath;


        File dir = new File(uploadDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        // 파일명 생성
        String fileName = UUID.randomUUID() + "_" + image.getOriginalFilename();

        // 실제 저장 경로
        File filePath = new File(dir, fileName);

        // 파일 저장
        image.transferTo(filePath);

        // 브라우저에서 접근할 URL (WebConfig에서 매핑한 /uploads/post/ 사용)
        String imageUrl = "/uploads/post/" + fileName;

        // 카테고리 변환
        PostCategory cat = PostCategory.valueOf(category.toUpperCase());


        // -----------------------------
        // 4. 게시글 저장
        // -----------------------------
        Post post = Post.builder()
                .content(content != null ? content.trim() : null)
                .imageUrl(imageUrl)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .writer(loginUser)
                .likeCount(0)
                .category(cat)
                .build();

        Post saved = postRepository.save(post);
        System.out.println("=== DEBUG SAVE ===");
        System.out.println("Original filename: " + image.getOriginalFilename());
        System.out.println("Saved filename: " + fileName);
        System.out.println("File path: " + filePath.getAbsolutePath());
        System.out.println("==================");


        // -----------------------------
        // 5. writer DTO 생성 (프론트에서 사용)
        // -----------------------------
        // username 통일해서 사용
        String loginUsername = buildUsername(loginUser);

        WriterDto writerDto = WriterDto.builder()
                .userSeq(loginUser.getUserSeq())
                .name(loginUser.getName())
                .username(loginUsername)
                .profileImage(loginUser.getProfileImage())
                .build();


        // -----------------------------
        // 6. 최종 응답 반환 (프론트가 post.writer.name 읽게)
        // -----------------------------
        return PostResponseDto.builder()
                .id(saved.getId())
                .content(saved.getContent())
                .imageUrl(saved.getImageUrl())
                .createdAt(saved.getCreatedAt().toString())
                .likeCount(saved.getLikeCount())
                .writer(writerDto)
                .build();
    }

    public List<PostResponseDto> getPostListDto(String myUsername, String category) {

        Signup loginUser = null;

        if (myUsername != null) {

            // username
            loginUser = signupRepository.findByUsername(myUsername).orElse(null);

            // id (일반 로그인)
            if (loginUser == null) {
                loginUser = signupRepository.findById(myUsername).orElse(null);
            }

            // provider + providerId (소셜 로그인)
            if (loginUser == null && myUsername.contains("_")) {
                String[] arr = myUsername.split("_", 2);
                String provider = arr[0];
                String providerId = arr[1];
                loginUser = signupRepository
                        .findByProviderAndProviderId(provider, providerId)
                        .orElse(null);
            }
        }

        List<Post> posts;
        if (category == null || category.equalsIgnoreCase("ALL")) {
            posts = postRepository.findAllByOrderByCreatedAtDesc();
        } else {
            PostCategory cat = PostCategory.valueOf(category.toUpperCase());
            posts = postRepository.findByCategoryOrderByCreatedAtDesc(cat);
        }

        final Signup loginUserFinal = loginUser;

        return posts.stream().map(post -> {

            Signup writer = post.getWriter();

            // ------ writer username 만들기 (공통 함수 사용 + DB 저장) ------
            String writerUsername = buildUsername(writer);

            if (writer.getUsername() == null || writer.getUsername().isBlank()) {
                writer.setUsername(writerUsername);
                signupRepository.save(writer);
            }

            // ------ 팔로우 여부 (username 정규화 후 비교) ------
            boolean isFollowing = false;
            String me = myUsername;
            String target = writerUsername;

            if (me != null && target != null) {
                isFollowing = followRepository
                        .existsByUserUsernameAndTargetUsername(me, target);
            }


            // ------ 좋아요 여부 ------
            boolean liked = false;
            if (loginUserFinal != null) {
                liked = postLikeRepository.existsByPostAndUser(post, loginUserFinal);
            }

            long commentCount = commentRepository.countByPost(post);  // 댓글 수

            WriterDto writerDto = WriterDto.builder()
                    .userSeq(writer.getUserSeq())
                    .name(writer.getName())
                    .username(writerUsername)
                    .profileImage(writer.getProfileImage())
                    .isFollowing(isFollowing)
                    .build();

            return PostResponseDto.builder()
                    .id(post.getId())
                    .content(post.getContent())
                    .imageUrl(post.getImageUrl())
                    .likeCount(post.getLikeCount())
                    .liked(liked)
                    .commentCount(commentCount)
                    .createdAt(post.getCreatedAt().toString())
                    .writer(writerDto)
                    .build();

        }).toList();
    }

    /** 좋아요 토글 */
    @Transactional
    public Map<String, Object> toggleLike(Long postId, HttpSession session) {

        Signup user = getLoginUserFromSession(session);
        if (user == null) {
            throw new IllegalStateException("로그인 필요");
        }

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글 없음"));

        boolean alreadyLiked = postLikeRepository.existsByPostAndUser(post, user);

        if (alreadyLiked) {
            // 좋아요 취소
            postLikeRepository.deleteByPostAndUser(post, user);
        } else {
            // 좋아요 추가
            postLikeRepository.save(new PostLike(post, user));
        }

        // 최신 좋아요 개수 계산
        int likeCount = postLikeRepository.countByPost(post);

        post.setLikeCount(likeCount);
        postRepository.save(post);

        boolean liked = !alreadyLiked;

        // 좋아요 개수 + 상태 같이 반환
        return Map.of(
                "likeCount", likeCount,
                "liked", liked
        );
    }

    /** 내 게시글 조회 */
    public List<PostResponseDto> getMyPostListDto(Signup user) {

        String me = buildUsername(user);
        String meNorm = normalizeUsername(me);

        return postRepository.findByWriter(user)
                .stream()
                .map(post -> {

                    String targetUsername = buildUsername(post.getWriter());
                    String targetNorm = normalizeUsername(targetUsername);

                    boolean liked =
                            postLikeRepository.existsByPostIdAndUserUsername(post.getId(), meNorm);

                    boolean isFollowing = false;
                    if (meNorm != null && targetNorm != null) {
                        isFollowing =
                                followRepository.existsByUserUsernameAndTargetUsername(
                                        meNorm,
                                        targetNorm
                                );
                    }

                    int commentCount = commentRepository.countByPost(post);

                    return new PostResponseDto(post, liked, isFollowing, commentCount);
                })
                .collect(Collectors.toList());
    }

    /** 게시글 상세 조회 */
    public Optional<Post> getPost(Long id) {
        return postRepository.findById(id);
    }

    /** 게시글 수정 */
    public PostResponseDto updatePost(Long id, String content, MultipartFile image, String category, HttpSession session) throws IOException {

        Signup loginUser = getLoginUserFromSession(session);
        if (loginUser == null) throw new IllegalStateException("로그인이 필요합니다.");

        Post post = postRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));

        if (!post.getWriter().getUserSeq().equals(loginUser.getUserSeq())) {
            throw new IllegalStateException("본인 게시글만 수정할 수 있습니다.");
        }

        // 내용 수정
        if (content != null) post.setContent(content.trim());

        // 신규 이미지 업로드
        if (image != null && !image.isEmpty()) {

            // 수정: 하드코딩된 경로 대신 주입받은 rootPath + postPath 사용
            String uploadDir = rootPath + postPath;

            File dir = new File(uploadDir);
            if (!dir.exists()) dir.mkdirs();

            String fileName = UUID.randomUUID() + "_" + image.getOriginalFilename();
            File filePath = new File(dir, fileName);
            image.transferTo(filePath);

            post.setImageUrl("/uploads/post/" + fileName);
        }

        if (category != null && !category.isBlank()) {
            PostCategory cat = PostCategory.valueOf(category.toUpperCase());
            post.setCategory(cat);
        }

        post.setUpdatedAt(LocalDateTime.now());
        Post saved = postRepository.save(post);

        String loginUsername = buildUsername(loginUser);

        WriterDto writerDto = WriterDto.builder()
                .userSeq(loginUser.getUserSeq())
                .name(loginUser.getName())
                .username(loginUsername)
                .profileImage(loginUser.getProfileImage())
                .build();

        return PostResponseDto.builder()
                .id(saved.getId())
                .content(saved.getContent())
                .imageUrl(saved.getImageUrl())
                .createdAt(saved.getCreatedAt().toString())
                .likeCount(saved.getLikeCount())
                .writer(writerDto)
                .build();
    }



    /** 게시글 삭제 */
    public void deletePost(Long id, HttpSession session) {
        Signup user = getLoginUserFromSession(session);
        Post post = postRepository.findById(id).orElseThrow();
        if (user != null && post.getWriter().getUserSeq().equals(user.getUserSeq())) {
            postRepository.delete(post);
        } else {
            throw new IllegalStateException("본인 게시글만 삭제할 수 있습니다.");
        }
    }

    public Signup getLoginUserFromSession(HttpSession session) {
        Object sessionUser = session.getAttribute("loginUser");

        // 일반 로그인: 엔티티 자체
        if (sessionUser instanceof Signup entity) {
            return entity;
        }

        // 소셜 로그인: DTO → 엔티티 변환
        if (sessionUser instanceof SignupDto dto) {
            return signupRepository.findByUserSeq(dto.getUserSeq())
                    .orElse(null);
        }

        return null;
    }

    // =======================
    //  username 공통 생성 로직
    // =======================
    private String buildUsername(Signup user) {
        if (user == null) return "unknownUser";

        // 1) DB에 이미 username이 있으면 최우선
        if (user.getUsername() != null && !user.getUsername().isBlank()) {
            return user.getUsername();
        }

        // 2) 자체 회원가입 id
        if (user.getId() != null && !user.getId().isBlank()) {
            return user.getId();
        }

        // 3) 소셜 로그인 provider + providerId
        if (user.getProvider() != null && user.getProviderId() != null) {
            return user.getProvider() + "_" + user.getProviderId();
        }

        // 4) oauthEmail 앞부분
        if (user.getOauthEmail() != null && !user.getOauthEmail().isBlank()) {
            return user.getOauthEmail().split("@")[0];
        }

        return "unknownUser";
    }

    private String normalizeUsername(String s) {
        return (s == null) ? null : s.trim().toLowerCase();
    }

}