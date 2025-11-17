package com.example.demo.Repository;

import com.example.demo.Domain.Common.Entity.Qna;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QnaRepository extends JpaRepository<Qna, String> {

    /* ============================================================
        전체 목록 조회 (최신순)
    ============================================================ */
    @Query("SELECT q FROM Qna q ORDER BY q.createdAt DESC")
    List<Qna> findAllByOrderByCreatedAtDesc();

    /* ============================================================
        카테고리별 조회
    ============================================================ */
    @Query("""
        SELECT q FROM Qna q
        WHERE q.category = :category
        ORDER BY q.createdAt DESC
    """)
    List<Qna> findByCategoryOrderByCreatedAtDesc(String category);

    /* ============================================================
        관리자: 미답변 조회
    ============================================================ */
    @Query("""
        SELECT q FROM Qna q
        WHERE q.adminAnswer IS NULL
        ORDER BY q.createdAt DESC
    """)
    List<Qna> findByAdminAnswerIsNullOrderByCreatedAtDesc();
}
