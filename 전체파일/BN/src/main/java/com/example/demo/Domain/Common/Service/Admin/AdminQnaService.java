package com.example.demo.Domain.Common.Service.Admin;

import com.example.demo.Domain.Common.Entity.Qna;
import com.example.demo.Repository.QnaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminQnaService {

    private final QnaRepository qnaRepository;

    /** ============================================================
     * 전체 QnA 개수
     * ============================================================ */
    public long countQna() {
        return qnaRepository.count();
    }

    /** ============================================================
     * 최근 QnA 조회 (limit)
     * ============================================================ */
    public List<Qna> findRecentQna(int limit) {
        return qnaRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .limit(limit)
                .toList();
    }

    /** ============================================================
     * 전체 QnA 목록 (최신순)
     * ============================================================ */
    public List<Qna> findAll() {
        return qnaRepository.findAllByOrderByCreatedAtDesc();
    }

    /** ============================================================
     * 단건 조회
     * ============================================================ */
    public Qna findById(String id) {
        return qnaRepository.findById(id).orElse(null);
    }

    /** ============================================================
     * QnA 삭제 (관리자 권한)
     * ============================================================ */
    public boolean deleteQna(String id) {

        if (!qnaRepository.existsById(id)) return false;

        qnaRepository.deleteById(id);
        log.info("[관리자 QnA 삭제 완료] id={}", id);
        return true;
    }

    // ============================================================
    //  관리자 답변 저장 / 수정
    // ============================================================

    /**
     * 답변 저장 또는 수정
     */
    public Qna saveAnswer(String qnaId, String adminName, String text) {

        Qna qna = qnaRepository.findById(qnaId)
                .orElseThrow(() -> new IllegalArgumentException("해당 QnA를 찾을 수 없습니다."));

        qna.setAdminAnswer(text);
        qna.setAdminName(adminName);
        qna.setAnswerAt(LocalDateTime.now());
        qna.setUpdatedAt(LocalDateTime.now());

        log.info("[관리자 답변 저장] qnaId={}, admin={}", qnaId, adminName);

        return qnaRepository.save(qna);
    }

    /**
     * 관리자 답변 삭제 (답변만 제거)
     */
    public Qna deleteAnswer(String qnaId) {

        Qna qna = qnaRepository.findById(qnaId)
                .orElseThrow(() -> new IllegalArgumentException("해당 QnA를 찾을 수 없습니다."));

        qna.setAdminAnswer(null);
        qna.setAdminName(null);
        qna.setAnswerAt(null);
        qna.setUpdatedAt(LocalDateTime.now());

        log.info("[관리자 답변 삭제] qnaId={}", qnaId);

        return qnaRepository.save(qna);
    }

    // ============================================================
    // 미답변 / 답변완료 필터링 기능
    // ============================================================

    /** 미답변 QnA 목록 */
    public List<Qna> findUnanswered() {
        return qnaRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .filter(q -> q.getAdminAnswer() == null || q.getAdminAnswer().isBlank())
                .toList();
    }

    /** 답변완료 QnA 목록 */
    public List<Qna> findAnswered() {

        return qnaRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .filter(q -> q.getAdminAnswer() != null && !q.getAdminAnswer().isBlank())
                .sorted(Comparator.comparing(Qna::getAnswerAt, Comparator.nullsLast(Comparator.naturalOrder())).reversed())
                .toList();
    }
}
