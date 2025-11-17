package com.example.demo.Domain.Common.Service;

import com.example.demo.Domain.Common.Dto.QnaAnswerDto;
import com.example.demo.Domain.Common.Dto.QnaRequestDto;
import com.example.demo.Domain.Common.Dto.QnaResponseDto;
import com.example.demo.Domain.Common.Entity.Qna;
import com.example.demo.Domain.Common.Entity.Signup;
import com.example.demo.Domain.Common.Service.QnaImageService;
import com.example.demo.Repository.QnaRepository;
import com.example.demo.Repository.SignupRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class QnaService {

    private final QnaRepository qnaRepository;
    private final SignupRepository signupRepository;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
    private final QnaImageService imageService;


    /* ============================================================
     * 유저: QnA 작성
     * ============================================================ */
    public QnaResponseDto create(QnaRequestDto dto) {

        Signup writer = signupRepository.findById(dto.getUserSeq())
                .orElseThrow(() -> new IllegalArgumentException("회원 정보를 찾을 수 없습니다."));

        // Base64 이미지 저장
        List<String> savedImages = imageService.saveBase64Images(dto.getImages());

        Qna qna = Qna.builder()
                .user(writer)
                .nickname(dto.getNickname())
                .writerPass(encoder.encode(dto.getWriterPass()))
                .title(dto.getTitle())
                .content(dto.getContent())
                .category(dto.getCategory())
                .secret(dto.isSecret())
                .images(savedImages)
                .links(dto.getLinks() != null ? dto.getLinks() : List.of())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        qnaRepository.save(qna);
        return QnaResponseDto.fromEntity(qna);
    }


    /* ============================================================
     * 유저: QnA 수정
     * ============================================================ */
    public QnaResponseDto update(QnaRequestDto dto) {

        Qna qna = qnaRepository.findById(dto.getId())
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));

        if (!encoder.matches(dto.getWriterPass(), qna.getWriterPass())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        // ------------------------------
        //   이미지 처리 (Base64 + 기존 URL 구분)
        // ------------------------------
        List<String> newUrls = imageService.saveBase64Images(dto.getImages());

        qna.setNickname(dto.getNickname());
        qna.setTitle(dto.getTitle());
        qna.setContent(dto.getContent());
        qna.setCategory(dto.getCategory());
        qna.setSecret(dto.isSecret());
        qna.setImages(newUrls); // ← URL만 저장
        qna.setLinks(dto.getLinks() != null ? dto.getLinks() : List.of());
        qna.setUpdatedAt(LocalDateTime.now());

        return QnaResponseDto.fromEntity(qna);
    }


    /* ============================================================
     * 유저: QnA 삭제
     * ============================================================ */
    public void delete(String id, String password) {

        Qna qna = qnaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));

        if (!encoder.matches(password, qna.getWriterPass())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        qnaRepository.delete(qna);
    }


    @Transactional(readOnly = true)
    public List<QnaResponseDto> findByCategory(String category) {
        return QnaResponseDto.fromEntities(
                qnaRepository.findByCategoryOrderByCreatedAtDesc(category)
        );
    }


    @Transactional(readOnly = true)
    public QnaResponseDto getDetail(String id) {

        Qna qna = qnaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));

        return QnaResponseDto.fromEntity(qna);
    }


    @Transactional(readOnly = true)
    public List<QnaResponseDto> findAll() {
        return QnaResponseDto.fromEntities(qnaRepository.findAllByOrderByCreatedAtDesc());
    }

    @Transactional(readOnly = true)
    public List<QnaResponseDto> findUnanswered() {
        return QnaResponseDto.fromEntities(
                qnaRepository.findByAdminAnswerIsNullOrderByCreatedAtDesc()
        );
    }


    public QnaResponseDto saveAnswer(QnaAnswerDto dto) {

        Qna qna = qnaRepository.findById(dto.getQnaId())
                .orElseThrow(() -> new IllegalArgumentException("문의가 존재하지 않습니다."));

        qna.setAdminAnswer(dto.getAnswer());
        qna.setAdminName(dto.getAdminName());
        qna.setAnswerAt(LocalDateTime.now());
        qna.setUpdatedAt(LocalDateTime.now());

        return QnaResponseDto.fromEntity(qna);
    }


    @Transactional(readOnly = true)
    public boolean checkPassword(String id, String rawPassword) {

        Qna qna = qnaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));

        return encoder.matches(rawPassword, qna.getWriterPass());
    }
}
