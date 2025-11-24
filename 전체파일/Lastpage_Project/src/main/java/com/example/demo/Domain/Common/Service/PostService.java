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

//        // -----------------------------
//        // 3. 이미지 저장
//        // -----------------------------
//        String uploadDir = System.getProperty("user.dir") + "/src/main/resources/static/uploads/";
//        File dir = new File(uploadDir);
//        if (!dir.exists()) dir.mkdirs();
//
//        String fileName = UUID.randomUUID() + "_" + image.getOriginalFilename();
//        File filePath = new File(uploadDir, fileName);
//        image.transferTo(filePath);
//
//        String imageUrl = "/uploads/" + fileName;
//
//        PostCategory cat = PostCategory.valueOf(category.toUpperCase());

        // -----------------------------
// -----------------------------
// 3. 이미지 저장 (통일된 구조: /lastpage_uploads/post/ )
// -----------------------------
        String uploadDir = System.getProperty("user.home") + "/lastpage_uploads/post/";

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

// 브라우저에서 접근할 URL
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

            String uploadDir = System.getProperty("user.home") + "/lastpage_uploads/post/";

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

//package com.example.demo.Domain.Common.Service;
//
//import com.example.demo.Domain.Common.Dto.PostResponseDto;
//import com.example.demo.Domain.Common.Dto.SignupDto;
//import com.example.demo.Domain.Common.Dto.WriterDto;
//import com.example.demo.Domain.Common.Entity.Post;
//import com.example.demo.Domain.Common.Entity.PostCategory;
//import com.example.demo.Domain.Common.Entity.PostLike;
//import com.example.demo.Domain.Common.Entity.Signup;
//import com.example.demo.Repository.CommentRepository;
//import com.example.demo.Repository.FollowRepository;
//import com.example.demo.Repository.PostLikeRepository;
//import com.example.demo.Repository.PostRepository;
//import com.example.demo.Repository.SignupRepository;
//import jakarta.servlet.http.HttpSession;
//import jakarta.transaction.Transactional;
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Service;
//import org.springframework.web.multipart.MultipartFile;
//
//import java.io.File;
//import java.io.IOException;
//import java.time.LocalDateTime;
//import java.time.format.DateTimeFormatter;
//import java.util.*;
//import java.util.stream.Collectors;
//
//@Service
//@RequiredArgsConstructor
//public class PostService {
//
//    private final PostRepository postRepository;
//    private final SignupRepository signupRepository;
//    private final FollowRepository followRepository;
//    private final PostLikeRepository postLikeRepository;
//    private final CommentRepository commentRepository;
//
//    private static final DateTimeFormatter DATE_FORMATTER =
//            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
//
//    /** 전체 게시글 (최신순 정렬) — 엔티티 반환은 내부 용도로만 사용 */
//    public List<Post> getAllPosts() {
//        List<Post> list = postRepository.findAll();
//        list.sort(Comparator.comparing(Post::getCreatedAt).reversed());
//        return list;
//    }
//
//    /** 게시글 등록 (사진 필수, 글 선택) */
//    public PostResponseDto savePost(
//            String content,
//            MultipartFile image,
//            String category,
//            HttpSession session
//    ) throws IOException {
//
//        // 1. 로그인 사용자 조회 (세션 → Signup 엔티티)
//        Signup loginUser = getLoginUserFromSession(session);
//        if (loginUser == null) {
//            throw new IllegalStateException("로그인이 필요합니다.");
//        }
//
//        // 2. 이미지 필수 체크
//        if (image == null || image.isEmpty()) {
//            throw new IllegalArgumentException("사진을 첨부해야 합니다.");
//        }
//
//        // 3. 이미지 저장
//        String uploadDir = System.getProperty("user.dir") + "/src/main/resources/static/uploads/";
//        File dir = new File(uploadDir);
//        if (!dir.exists()) dir.mkdirs();
//
//        String fileName = UUID.randomUUID() + "_" + image.getOriginalFilename();
//        File filePath = new File(uploadDir, fileName);
//        image.transferTo(filePath);
//
//        String imageUrl = "/uploads/" + fileName;
//
//        // 4. 카테고리 변환
//        PostCategory cat = PostCategory.valueOf(category.toUpperCase());
//
//        // 5. 게시글 생성 및 저장
//        Post post = Post.builder()
//                .content(content != null ? content.trim() : null)
//                .imageUrl(imageUrl)
//                .createdAt(LocalDateTime.now())
//                .updatedAt(LocalDateTime.now())
//                .writer(loginUser)
//                .likeCount(0)
//                .category(cat)
//                .build();
//
//        Post saved = postRepository.save(post);
//
//        // 6. username 통일
//        String loginUsername = buildUsername(loginUser);
//        if (loginUser.getUsername() == null || loginUser.getUsername().isBlank()) {
//            loginUser.setUsername(loginUsername);
//            signupRepository.save(loginUser);
//        }
//
//        WriterDto writerDto = WriterDto.builder()
//                .userSeq(loginUser.getUserSeq())
//                .name(loginUser.getName())
//                .username(loginUsername)
//                .profileImage(loginUser.getProfileImage())
//                .build();
//
//        return PostResponseDto.builder()
//                .id(saved.getId())
//                .content(saved.getContent())
//                .imageUrl(saved.getImageUrl())
//                .createdAt(saved.getCreatedAt().format(DATE_FORMATTER))
//                .likeCount(saved.getLikeCount())
//                .writer(writerDto)
//                .commentCount(0L)
//                .liked(false)
//                .category(saved.getCategory().name())
//                .build();
//    }
//
//    /** 게시글 목록 DTO (내가 좋아요 눌렀는지, 팔로우 여부 포함) */
//    public List<PostResponseDto> getPostListDto(String myUsername, String category) {
//
//        // 1. 로그인 사용자(엔티티) 조회
//        Signup loginUser = findSignupByAnyUsername(myUsername);
//        final Signup loginUserFinal = loginUser;
//
//        // 2. 게시글 목록 조회
//        List<Post> posts;
//        if (category == null || category.equalsIgnoreCase("ALL")) {
//            posts = postRepository.findAllByOrderByCreatedAtDesc();
//        } else {
//            PostCategory cat = PostCategory.valueOf(category.toUpperCase());
//            posts = postRepository.findByCategoryOrderByCreatedAtDesc(cat);
//        }
//
//        // 3. DTO 변환
//        return posts.stream().map(post -> {
//
//            Signup writer = post.getWriter();
//
//            // writer username 계산 및 DB에 저장(없을 경우)
//            String writerUsername = buildUsername(writer);
//            if (writer.getUsername() == null || writer.getUsername().isBlank()) {
//                writer.setUsername(writerUsername);
//                signupRepository.save(writer);
//            }
//
//            // 팔로우 여부
//            boolean isFollowing = false;
//            if (myUsername != null && writerUsername != null) {
//                String meNorm = normalizeUsername(myUsername);
//                String targetNorm = normalizeUsername(writerUsername);
//                if (meNorm != null && targetNorm != null) {
//                    isFollowing = followRepository
//                            .existsByUserUsernameAndTargetUsername(meNorm, targetNorm);
//                }
//            }
//
//            // 좋아요 여부
//            boolean liked = false;
//            if (loginUserFinal != null) {
//                liked = postLikeRepository.existsByPostAndUser(post, loginUserFinal);
//            }
//
//            long commentCount = commentRepository.countByPost(post);
//
//            WriterDto writerDto = WriterDto.builder()
//                    .userSeq(writer.getUserSeq())
//                    .name(writer.getName())
//                    .username(writerUsername)
//                    .profileImage(writer.getProfileImage())
//                    .isFollowing(isFollowing)
//                    .build();
//
//            return PostResponseDto.builder()
//                    .id(post.getId())
//                    .content(post.getContent())
//                    .imageUrl(post.getImageUrl())
//                    .likeCount(post.getLikeCount())
//                    .liked(liked)
//                    .commentCount(commentCount)
//                    .createdAt(post.getCreatedAt() != null
//                            ? post.getCreatedAt().format(DATE_FORMATTER)
//                            : null)
//                    .writer(writerDto)
//                    .category(post.getCategory().name())
//                    .build();
//
//        }).toList();
//    }
//
//    /** 좋아요 토글 */
//    @Transactional
//    public Map<String, Object> toggleLike(Long postId, HttpSession session) {
//
//        Signup user = getLoginUserFromSession(session);
//        if (user == null) {
//            throw new IllegalStateException("로그인 필요");
//        }
//
//        Post post = postRepository.findById(postId)
//                .orElseThrow(() -> new IllegalArgumentException("게시글 없음"));
//
//        boolean alreadyLiked = postLikeRepository.existsByPostAndUser(post, user);
//
//        if (alreadyLiked) {
//            // 좋아요 취소
//            postLikeRepository.deleteByPostAndUser(post, user);
//        } else {
//            // 좋아요 추가
//            postLikeRepository.save(new PostLike(post, user));
//        }
//
//        int likeCount = postLikeRepository.countByPost(post);
//        post.setLikeCount(likeCount);
//        postRepository.save(post);
//
//        boolean liked = !alreadyLiked;
//
//        return Map.of(
//                "likeCount", likeCount,
//                "liked", liked
//        );
//    }
//
//    /** 내 게시글 조회 */
//    public List<PostResponseDto> getMyPostListDto(Signup user) {
//
//        String me = buildUsername(user);
//        String meNorm = normalizeUsername(me);
//
//        return postRepository.findByWriter(user)
//                .stream()
//                .map(post -> {
//
//                    String targetUsername = buildUsername(post.getWriter());
//                    String targetNorm = normalizeUsername(targetUsername);
//
//                    boolean liked = false;
//                    if (meNorm != null) {
//                        liked = postLikeRepository.existsByPostIdAndUserUsername(
//                                post.getId(),
//                                meNorm
//                        );
//                    }
//
//                    boolean isFollowing = false;
//                    if (meNorm != null && targetNorm != null) {
//                        isFollowing = followRepository
//                                .existsByUserUsernameAndTargetUsername(
//                                        meNorm,
//                                        targetNorm
//                                );
//                    }
//
//                    long commentCount = commentRepository.countByPost(post);
//
//                    return new PostResponseDto(post, liked, isFollowing, commentCount);
//                })
//                .collect(Collectors.toList());
//    }
//
//    /** 게시글 상세 조회: 엔티티 대신 DTO로 반환해서 직렬화 문제 방지 */
//    public Optional<PostResponseDto> getPostDto(Long id, String myUsername) {
//
//        Signup loginUser = findSignupByAnyUsername(myUsername);
//        String meNorm = myUsername != null ? normalizeUsername(myUsername) : null;
//
//        return postRepository.findById(id).map(post -> {
//
//            Signup writer = post.getWriter();
//            String writerUsername = buildUsername(writer);
//            if (writer.getUsername() == null || writer.getUsername().isBlank()) {
//                writer.setUsername(writerUsername);
//                signupRepository.save(writer);
//            }
//
//            // 팔로우 여부
//            boolean isFollowing = false;
//            if (meNorm != null && writerUsername != null) {
//                String targetNorm = normalizeUsername(writerUsername);
//                if (targetNorm != null) {
//                    isFollowing = followRepository
//                            .existsByUserUsernameAndTargetUsername(meNorm, targetNorm);
//                }
//            }
//
//            // 좋아요 여부
//            boolean liked = false;
//            if (loginUser != null) {
//                liked = postLikeRepository.existsByPostAndUser(post, loginUser);
//            }
//
//            long commentCount = commentRepository.countByPost(post);
//
//            return new PostResponseDto(post, liked, isFollowing, commentCount);
//        });
//    }
//
//    /** 게시글 수정 */
//    public PostResponseDto updatePost(
//            Long id,
//            String content,
//            MultipartFile image,
//            String category,
//            HttpSession session
//    ) throws IOException {
//
//        // 로그인 사용자
//        Signup loginUser = getLoginUserFromSession(session);
//        if (loginUser == null) throw new IllegalStateException("로그인이 필요합니다.");
//
//        // 기존 게시글
//        Post post = postRepository.findById(id)
//                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));
//
//        // 본인 글인지 확인
//        if (!post.getWriter().getUserSeq().equals(loginUser.getUserSeq())) {
//            throw new IllegalStateException("본인 게시글만 수정할 수 있습니다.");
//        }
//
//        // 내용 수정
//        if (content != null) {
//            post.setContent(content.trim());
//        }
//
//        // 이미지 수정 (선택)
//        if (image != null && !image.isEmpty()) {
//
//            String uploadDir = System.getProperty("user.dir") + "/src/main/resources/static/uploads/";
//            File dir = new File(uploadDir);
//            if (!dir.exists()) dir.mkdirs();
//
//            String fileName = UUID.randomUUID() + "_" + image.getOriginalFilename();
//            File filePath = new File(uploadDir, fileName);
//            image.transferTo(filePath);
//
//            post.setImageUrl("/uploads/" + fileName);
//        }
//
//        // 카테고리 수정
//        if (category != null && !category.isBlank()) {
//            PostCategory cat = PostCategory.valueOf(category.toUpperCase());
//            post.setCategory(cat);
//        }
//
//        post.setUpdatedAt(LocalDateTime.now());
//        Post saved = postRepository.save(post);
//
//        String loginUsername = buildUsername(loginUser);
//        if (loginUser.getUsername() == null || loginUser.getUsername().isBlank()) {
//            loginUser.setUsername(loginUsername);
//            signupRepository.save(loginUser);
//        }
//
//        WriterDto writerDto = WriterDto.builder()
//                .userSeq(loginUser.getUserSeq())
//                .name(loginUser.getName())
//                .username(loginUsername)
//                .profileImage(loginUser.getProfileImage())
//                .build();
//
//        long commentCount = commentRepository.countByPost(saved);
//        boolean liked = postLikeRepository.existsByPostAndUser(saved, loginUser);
//
//        return PostResponseDto.builder()
//                .id(saved.getId())
//                .content(saved.getContent())
//                .imageUrl(saved.getImageUrl())
//                .createdAt(saved.getCreatedAt() != null
//                        ? saved.getCreatedAt().format(DATE_FORMATTER)
//                        : null)
//                .likeCount(saved.getLikeCount())
//                .writer(writerDto)
//                .commentCount(commentCount)
//                .liked(liked)
//                .category(saved.getCategory().name())
//                .build();
//    }
//
//    /** 게시글 삭제 */
//    public void deletePost(Long id, HttpSession session) {
//        Signup user = getLoginUserFromSession(session);
//        if (user == null) {
//            throw new IllegalStateException("로그인이 필요합니다.");
//        }
//
//        Post post = postRepository.findById(id)
//                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));
//
//        if (post.getWriter().getUserSeq().equals(user.getUserSeq())) {
//            postRepository.delete(post);
//        } else {
//            throw new IllegalStateException("본인 게시글만 삭제할 수 있습니다.");
//        }
//    }
//
//    /** 세션에서 로그인 사용자 엔티티 통일 */
//    public Signup getLoginUserFromSession(HttpSession session) {
//        Object sessionUser = session.getAttribute("loginUser");
//
//        // 일반 로그인: 엔티티 자체가 들어있는 경우
//        if (sessionUser instanceof Signup entity) {
//            // 여기서 바로 세션에 엔티티를 넣는 방식이 근본 원인은 맞는데,
//            // 우선은 읽기만 하고, 되도록 새 엔티티를 읽어서 쓰는 패턴으로 유지
//            return signupRepository.findByUserSeq(entity.getUserSeq())
//                    .orElse(entity);
//        }
//
//        // 소셜 로그인: DTO → 엔티티 조회
//        if (sessionUser instanceof SignupDto dto) {
//            if (dto.getUserSeq() == null) return null;
//            return signupRepository.findByUserSeq(dto.getUserSeq())
//                    .orElse(null);
//        }
//
//        // 혹시 userSeq만 세션에 따로 넣어둔 경우도 고려
//        Object userSeqObj = session.getAttribute("userSeq");
//        if (userSeqObj instanceof String userSeq) {
//            return signupRepository.findByUserSeq(userSeq).orElse(null);
//        }
//
//        return null;
//    }
//
//    // =======================
//    // username 공통 생성 로직
//    // =======================
//    private String buildUsername(Signup user) {
//        if (user == null) return "unknownUser";
//
//        // 1) DB에 이미 username이 있으면 최우선
//        if (user.getUsername() != null && !user.getUsername().isBlank()) {
//            return user.getUsername();
//        }
//
//        // 2) 자체 회원가입 id
//        if (user.getId() != null && !user.getId().isBlank()) {
//            return user.getId();
//        }
//
//        // 3) 소셜 로그인 provider + providerId
//        if (user.getProvider() != null && user.getProviderId() != null) {
//            return user.getProvider() + "_" + user.getProviderId();
//        }
//
//        // 4) oauthEmail 앞부분
//        if (user.getOauthEmail() != null && !user.getOauthEmail().isBlank()) {
//            return user.getOauthEmail().split("@")[0];
//        }
//
//        return "unknownUser";
//    }
//
//    private String normalizeUsername(String s) {
//        return (s == null) ? null : s.trim().toLowerCase();
//    }
//
//    /**
//     * username / id / provider_providerId 등 어떤 형식이든
//     * 들어왔을 때 최대한 Signup 엔티티를 찾아보는 헬퍼
//     */
//    private Signup findSignupByAnyUsername(String myUsername) {
//        if (myUsername == null || myUsername.isBlank()) return null;
//
//        String me = myUsername;
//        Signup loginUser = null;
//
//        // username
//        loginUser = signupRepository.findByUsername(me).orElse(null);
//
//        // id (일반 로그인)
//        if (loginUser == null) {
//            loginUser = signupRepository.findById(me).orElse(null);
//        }
//
//        // provider + providerId (소셜 로그인)
//        if (loginUser == null && me.contains("_")) {
//            String[] arr = me.split("_", 2);
//            String provider = arr[0];
//            String providerId = arr.length > 1 ? arr[1] : null;
//            if (providerId != null) {
//                loginUser = signupRepository
//                        .findByProviderAndProviderId(provider, providerId)
//                        .orElse(null);
//            }
//        }
//
//        return loginUser;
//    }
//}
