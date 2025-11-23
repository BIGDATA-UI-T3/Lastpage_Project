//package com.example.demo.Domain.Common.Entity;
//
//import com.fasterxml.jackson.annotation.JsonIgnore;
//import jakarta.persistence.*;
//import lombok.AllArgsConstructor;
//import lombok.Builder;
//import lombok.Data;
//import lombok.NoArgsConstructor;
//import org.hibernate.annotations.GenericGenerator;
//
//import java.io.Serial;
//import java.io.Serializable;
//import java.time.LocalDateTime;
//import java.util.List;
//
//@Entity
//@Table(name = "user_info")
//@Data
//@AllArgsConstructor
//@NoArgsConstructor
//@Builder
//public class Signup implements Serializable {
//    @Serial
//    private static final long serialVersionUID = 1L;
//
//    /** 기본키 */
//    @Id
//    @GeneratedValue(generator = "uuid2")
//    @GenericGenerator(name = "uuid2", strategy = "org.hibernate.id.UUIDGenerator")
//    @Column(name = "user_seq", nullable = false, updatable = false, unique = true, length = 36)
//    private String userSeq;
//
//    @Enumerated(EnumType.STRING)
//    @Column(nullable = false)
//    private Role role;     // ROLE_USER, ROLE_ADMIN
//
//
//
//    /**  일반 회원가입 및 공통 정보 */
//    private String name;
//    private String id; // 일반 회원 ID (소셜 회원은 null 가능)
//    private String password;
//    private String confirm_password;
//    @Column(name = "email_id")
//    private String emailId;
//    @Column(name = "email_domain")
//    private String emailDomain;
//    private Long year;
//    private Long month;
//    private Long day;
//
//    @Enumerated(EnumType.STRING)
//    private Gender gender;
//    private String phone_num;
//    private String sms_auth_number;
//    private LocalDateTime created_at;
//    private LocalDateTime updated_at;
//
//    /**  소셜 로그인(OAuth) 전용 필드 */
//    @Column(name = "provider")
//    private String provider;
//    @Column(name = "provider_id")// kakao, naver, google
//    private String providerId;    // 각 플랫폼의 고유 사용자 ID
//    private String profileImage;  // 프로필 이미지 URL
//    private String oauthEmail;    // 소셜 로그인에서 받은 이메일
//
//    // 커뮤니티 프로필
//    private String bio;
//
//    //  post 게시
//    @OneToMany(mappedBy = "writer", fetch = FetchType.LAZY)
//    @JsonIgnore
//    private List<Post> posts;
//
//    @OneToMany(mappedBy = "writer", fetch = FetchType.LAZY)
//    @JsonIgnore
//    private List<Comment> comments;
//
//    //  follow
//    private String username;
//}

package com.example.demo.Domain.Common.Entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "user_info")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Signup implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "user_seq", nullable = false, updatable = false, unique = true, length = 36)
    private String userSeq;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    private String name;
    private String id;
    private String password;
    private String confirm_password;

    @Column(name = "email_id")
    private String emailId;

    @Column(name = "email_domain")
    private String emailDomain;

    private Long year;
    private Long month;
    private Long day;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    private String phone_num;
    private String sms_auth_number;
    private LocalDateTime created_at;
    private LocalDateTime updated_at;

    private String provider;
    private String providerId;
    private String profileImage;
    private String oauthEmail;

    private String bio;

    @OneToMany(mappedBy = "writer", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Post> posts;



    private String username;

    @Override
    public String toString() {
        return "Signup(userSeq=" + userSeq +
                ", name=" + name +
                ", id=" + id +
                ", role=" + role +
                ")";
    }
}
