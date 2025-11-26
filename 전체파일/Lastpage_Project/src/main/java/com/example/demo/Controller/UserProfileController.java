package com.example.demo.Controller;

import com.example.demo.Domain.Common.Dto.ProfileResponseDto;
import com.example.demo.Domain.Common.Dto.SignupDto;
import com.example.demo.Domain.Common.Entity.Signup;
import com.example.demo.Repository.FollowRepository;
import com.example.demo.Repository.SignupRepository;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user")
public class UserProfileController {

    //  필드 추가
    @Value("${file.upload.root}")
    private String rootPath;

    //  profile 이미지는 ourpage/ 경로에 저장한다고 가정
    @Value("${file.upload.post}")
    private String profileImagePath;

    private final FollowRepository followRepository;
    private final SignupRepository signupRepository;

    // =========================================
    // 1. 프로필 조회
    // =========================================
    @GetMapping("/profile")
    public ProfileResponseDto getUserProfile(HttpSession session) {

        Object sessionUser = session.getAttribute("loginUser");
        ProfileResponseDto dto = new ProfileResponseDto();

        // 로그인 X
        if (sessionUser == null) {
            dto.setLogin(false);
            return dto;
        }

        // 우선 엔티티로 통일해서 가져오기
        Signup userEntity = null;

        if (sessionUser instanceof Signup entity) {
            userEntity = entity;
        } else if (sessionUser instanceof SignupDto signupDto) {
            if (signupDto.getUserSeq() != null) {
                userEntity = signupRepository.findByUserSeq(signupDto.getUserSeq())
                        .orElse(null);
            }
        }

        // 엔티티를 못 가져오면(이상 케이스) 그냥 세션 DTO 기준으로라도 내려줌
        dto.setLogin(true);

        // ----------------------------
        // 1) 엔티티 기준(정상 흐름)
        // ----------------------------
        if (userEntity != null) {
            dto.setName(userEntity.getName());
            dto.setBio(userEntity.getBio());
            dto.setProfileImage(userEntity.getProfileImage());

            // username 계산 (PostService랑 로직 맞춰줌)
            String username;
            if (userEntity.getUsername() != null && !userEntity.getUsername().isEmpty()) {
                username = userEntity.getUsername();
            } else if (userEntity.getId() != null && !userEntity.getId().isEmpty()) {
                username = userEntity.getId();
            } else if (userEntity.getProvider() != null && userEntity.getProviderId() != null) {
                username = userEntity.getProvider() + "_" + userEntity.getProviderId();
            } else if (userEntity.getOauthEmail() != null) {
                username = userEntity.getOauthEmail().split("@")[0];
            } else {
                username = "unknownUser";
            }

            dto.setUsername(username);
            dto.setFollowerCount(followRepository.countByTargetUsername(username));
            dto.setFollowingCount(followRepository.countByUserUsername(username));

            return dto;
        }

        // ----------------------------
        // 2) 엔티티가 없을 때, 세션 DTO 기준으로 fallback
        // ----------------------------
        if (sessionUser instanceof SignupDto signupDto) {
            dto.setName(signupDto.getName());
            dto.setBio(signupDto.getBio());
            dto.setProfileImage(signupDto.getProfileImage());

            String username = signupDto.getUsername();
            if (username == null || username.isEmpty()) {
                if (signupDto.getProvider() != null && signupDto.getProviderId() != null) {
                    username = signupDto.getProvider() + "_" + signupDto.getProviderId();
                } else if (signupDto.getOauthEmail() != null) {
                    username = signupDto.getOauthEmail().split("@")[0];
                } else if (signupDto.getId() != null) {
                    username = signupDto.getId();
                } else {
                    username = "unknownUser";
                }
            }

            dto.setUsername(username);
            dto.setFollowerCount(followRepository.countByTargetUsername(username));
            dto.setFollowingCount(followRepository.countByUserUsername(username));
        }

        return dto;
    }

    // =========================================
    // 2. 프로필 수정
    // =========================================
    @PostMapping("/profile/edit")
    public ProfileResponseDto editProfile(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String bio,
            @RequestParam(required = false) MultipartFile image,
            HttpSession session
    ) throws IOException {

        // 세션에서 Signup 엔티티/DTO를 받아서 엔티티로 통일
        Signup user = getUserFromSession(session);
        if (user == null) {
            throw new IllegalStateException("로그인 필요");
        }

        // ---------- 이름 / 소개글 ----------
        if (name != null) user.setName(name);
        if (bio != null) user.setBio(bio);

        // ---------- 프로필 이미지 업로드 ----------
        if (image != null && !image.isEmpty()) {

            // 🛠️ 수정: 하드코딩 대신 주입받은 rootPath + profileImagePath 사용
            String uploadDir = rootPath + profileImagePath;

            File dir = new File(uploadDir);
            if (!dir.exists()) dir.mkdirs();

            //  수정: 한글 파일명 방지 및 이미지 URL 경로 수정
            String originalFilename = image.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String fileName = UUID.randomUUID().toString() + extension; // UUID + 확장자만 사용

            File filePath = new File(uploadDir, fileName);
            image.transferTo(filePath);

            //  수정: URL에 슬래시(/) 추가 및 post 대신 ourpage 경로 사용
            String imageUrl = "/uploads/post/" + fileName;
            user.setProfileImage(imageUrl);
        }

        // DB 저장
        signupRepository.save(user);

        // 세션 갱신 (이제 엔티티 기준으로 유지)
        session.setAttribute("loginUser", user);

        // 프론트에 내려줄 DTO
        ProfileResponseDto dto = new ProfileResponseDto();
        dto.setLogin(true);
        dto.setName(user.getName());
        dto.setBio(user.getBio());
        dto.setProfileImage(user.getProfileImage());

        String username;
        if (user.getUsername() != null && !user.getUsername().isEmpty()) {
            username = user.getUsername();
        } else if (user.getId() != null && !user.getId().isEmpty()) {
            username = user.getId();
        } else if (user.getProvider() != null && user.getProviderId() != null) {
            username = user.getProvider() + "_" + user.getProviderId();
        } else if (user.getOauthEmail() != null) {
            username = user.getOauthEmail().split("@")[0];
        } else {
            username = "unknownUser";
        }

        dto.setUsername(username);
        dto.setFollowerCount(followRepository.countByTargetUsername(username));
        dto.setFollowingCount(followRepository.countByUserUsername(username));

        return dto;
    }

    // =========================================
    // 세션에서 Signup 통일해서 꺼내는 헬퍼
    // =========================================
    private Signup getUserFromSession(HttpSession session) {
        Object sessionUser = session.getAttribute("loginUser");

        if (sessionUser instanceof Signup entity) {
            return entity;
        }

        if (sessionUser instanceof SignupDto dto) {
            if (dto.getUserSeq() == null) return null;
            return signupRepository.findByUserSeq(dto.getUserSeq()).orElse(null);
        }

        return null;
    }
}
