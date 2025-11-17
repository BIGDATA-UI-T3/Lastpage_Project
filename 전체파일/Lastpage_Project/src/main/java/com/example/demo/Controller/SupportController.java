package com.example.demo.Controller;

import com.example.demo.Domain.Common.Dto.QnaAnswerDto;
import com.example.demo.Domain.Common.Dto.QnaRequestDto;
import com.example.demo.Domain.Common.Dto.QnaResponseDto;
import com.example.demo.Domain.Common.Service.QnaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/supportService")
public class SupportController {

    private final QnaService qnaService;

    /** ==========================================================
     * 고객센터 메인 페이지
     * ========================================================== */
    @GetMapping
    public String supportPage() {
        return "support/Support";
    }



    /** ==========================================================
     *  [유저] QnA 단건 상세 조회
     * ========================================================== */
    @GetMapping("/api/qna/{id}")
    @ResponseBody
    public QnaResponseDto getDetail(@PathVariable String id) {
        return qnaService.getDetail(id);
    }

    /** ==========================================================
     *  [유저] QnA 작성
     * POST /supportService/api/qna
     * ========================================================== */
    @PostMapping("/api/qna")
    @ResponseBody
    public QnaResponseDto create(@RequestBody QnaRequestDto dto) {
        return qnaService.create(dto);
    }

    /** ==========================================================
     *  [유저] QnA 수정
     * PUT /supportService/api/qna
     * ========================================================== */
    @PutMapping("/api/qna")
    @ResponseBody
    public QnaResponseDto update(@RequestBody QnaRequestDto dto) {
        return qnaService.update(dto);
    }

    /** ==========================================================
     *  [유저] QnA 목록 조회 (카테고리 필터)
     *  GET /supportService/api/qna?category=xxx
     * ========================================================== */
    @GetMapping("/api/qna")
    @ResponseBody
    public List<QnaResponseDto> getQnaList(@RequestParam(defaultValue = "ALL") String category) {

        if (category.equalsIgnoreCase("ALL")) {
            return qnaService.findAll();
        }

        return qnaService.findByCategory(category);
    }



    /** ==========================================================
     *  [유저] QnA 삭제
     * DELETE /supportService/api/qna/{id}?password=xxx
     * ========================================================== */
    @DeleteMapping("/api/qna/{id}")
    @ResponseBody
    public ResponseEntity<String> delete(
            @PathVariable String id,
            @RequestParam String password
    ) {
        qnaService.delete(id, password);
        return ResponseEntity.ok("삭제되었습니다.");
    }

    /** ==========================================================
     *  [유저] 비밀번호 확인 API
     * GET /supportService/api/qna/check/{id}?password=xxx
     * ========================================================== */
    @GetMapping("/api/qna/check/{id}")
    @ResponseBody
    public boolean checkPassword(
            @PathVariable String id,
            @RequestParam String password
    ) {
        return qnaService.checkPassword(id, password);
    }

    /** ==========================================================
     *  [관리자] 전체 문의 조회 (대시보드용)
     * ========================================================== */
    @GetMapping("/api/admin/qna")
    @ResponseBody
    public List<QnaResponseDto> getAllQna() {
        return qnaService.findAll();
    }

    /** ==========================================================
     *  [관리자] 답변 등록 및 수정
     * ========================================================== */
    @PostMapping("/api/admin/qna/answer")
    @ResponseBody
    public QnaResponseDto answer(@RequestBody QnaAnswerDto dto) {
        return qnaService.saveAnswer(dto);
    }
}
