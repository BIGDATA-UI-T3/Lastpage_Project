package com.example.demo.Domain.Common.Entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "qna_board")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Qna {

    /** 게시글 PK */
    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "qna_id", nullable = false, updatable = false, unique = true)
    private String id;

    /** 회원(FK) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_seq", referencedColumnName = "user_seq", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Signup user;

    /** 작성자 닉네임 */
    @Column(length = 50)
    private String nickname;

    /** 비밀번호 (암호화 저장 권장) */
    @Column(nullable = false, length = 60)
    private String writerPass;

    /** 제목 */
    @Column(nullable = false, length = 150)
    private String title;

    /** 내용 */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    /** 카테고리 */
    @Column(nullable = false, length = 50)
    private String category;

    /** 비공개 여부 */
    @Column(nullable = false)
    private boolean secret;

    /** 이미지 최대 3개 */


    @ElementCollection
    @CollectionTable(name = "qna_images", joinColumns = @JoinColumn(name = "qna_id"))
    @Column(name = "image_url", columnDefinition = "LONGTEXT")  // ← 이 부분 추가/수정
    private List<String> images;

    /** 링크 여러 개 */
    @ElementCollection
    @CollectionTable(name = "qna_links", joinColumns = @JoinColumn(name = "qna_id"))
    @Column(name = "link_url")
    private List<String> links;

    /** 관리자 답변 */
    @Column(columnDefinition = "TEXT")
    private String adminAnswer;

    private String adminName;

    private LocalDateTime answerAt;

    /** 생성/수정 */
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}
