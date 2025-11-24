package com.example.demo.Domain.Common.Service;

import com.example.demo.Domain.Common.Dto.CommentResponseDto;
import com.example.demo.Domain.Common.Entity.Comment;
import com.example.demo.Domain.Common.Entity.Post;
import com.example.demo.Domain.Common.Entity.Signup;
import com.example.demo.Repository.CommentRepository;
import com.example.demo.Repository.PostRepository;
import com.example.demo.Repository.SignupRepository;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final SignupRepository signupRepository;

    private final DateTimeFormatter formatter =
            DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm");

    // 세션에서 로그인 유저 꺼내기 (SignupDto, Signup 둘 다 대응)
    private Signup getLoginUser(HttpSession session) {
        Object sessionUser = session.getAttribute("loginUser");

        if (sessionUser instanceof Signup entity) {
            return entity;
        }

        // SignupDto → Signup (간단 변환)
        if (sessionUser instanceof com.example.demo.Domain.Common.Dto.SignupDto dto) {
            return signupRepository.findByUserSeq(dto.getUserSeq())
                    .orElse(null);
        }

        return null;
    }

    private String resolveUsername(Signup user) {
        if (user.getUsername() != null && !user.getUsername().isEmpty()) {
            return user.getUsername();
        } else if (user.getId() != null && !user.getId().isEmpty()) {
            return user.getId();
        } else if (user.getProvider() != null && user.getProviderId() != null) {
            return user.getProvider() + "_" + user.getProviderId();
        } else if (user.getOauthEmail() != null) {
            return user.getOauthEmail().split("@")[0];
        } else {
            return "unknownUser";
        }
    }

    public List<CommentResponseDto> getComments(Long postId, HttpSession session) {
        Signup me = getLoginUser(session);

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));

        List<Comment> comments = commentRepository.findByPostOrderByCreatedAtAsc(post);

        return comments.stream().map(c -> {
            Signup writer = c.getWriter();
            String username = resolveUsername(writer);

            boolean mine = (me != null
                    && me.getUserSeq().equals(writer.getUserSeq()));

            return CommentResponseDto.builder()
                    .id(c.getId())
                    .content(c.getContent())
                    .writerName(writer.getName())
                    .writerUsername(username)
                    .createdAt(c.getCreatedAt().format(formatter))
                    .mine(mine)
                    .build();
        }).toList();
    }

    public CommentResponseDto addComment(Long postId, String content, HttpSession session) {
        Signup me = getLoginUser(session);
        if (me == null) {
            throw new IllegalStateException("로그인이 필요합니다.");
        }

        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("댓글 내용을 입력해주세요.");
        }

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));

        Comment comment = Comment.builder()
                .post(post)
                .writer(me)
                .content(content.trim())
                .createdAt(LocalDateTime.now())
                .build();

        Comment saved = commentRepository.save(comment);

        return CommentResponseDto.builder()
                .id(saved.getId())
                .content(saved.getContent())
                .writerName(me.getName())
                .writerUsername(resolveUsername(me))
                .createdAt(saved.getCreatedAt().format(formatter))
                .mine(true)
                .build();
    }

    public void deleteComment(Long commentId, HttpSession session) {
        Signup me = getLoginUser(session);
        if (me == null) {
            throw new IllegalStateException("로그인이 필요합니다.");
        }

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("댓글을 찾을 수 없습니다."));

        if (!comment.getWriter().getUserSeq().equals(me.getUserSeq())) {
            throw new IllegalStateException("본인 댓글만 삭제할 수 있습니다.");
        }

        commentRepository.delete(comment);
    }
}
