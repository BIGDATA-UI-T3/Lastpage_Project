package com.example.demo.Controller;

import com.example.demo.Domain.Common.Dto.QnaAnswerDto;
import com.example.demo.Domain.Common.Dto.QnaResponseDto;
import com.example.demo.Domain.Common.Service.Admin.AdminQnaService;
import com.example.demo.Domain.Common.Service.QnaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/admin/qna")
public class AdminQnaController {

    private final AdminQnaService adminQnaService;
    private final QnaService qnaService;

    /** ---------------------------------------------
     * 관리자 QnA 대시보드
     * --------------------------------------------- */
    @GetMapping
    public String qnaDashboard(Model model) {

        List<QnaResponseDto> qnaList = qnaService.findAll();

        model.addAttribute("qnaList", qnaList);
        model.addAttribute("total", qnaList.size());

        model.addAttribute("unanswered", qnaList.stream()
                .filter(q -> q.getAdminAnswer() == null || q.getAdminAnswer().isBlank())
                .count());

        model.addAttribute("answered", qnaList.stream()
                .filter(q -> q.getAdminAnswer() != null && !q.getAdminAnswer().isBlank())
                .count());

        return "admin/AdminQna";
    }

    /** ---------------------------------------------
     * 관리자 QnA 상세 페이지 (답변 작성/수정 포함)
     * --------------------------------------------- */
    @GetMapping("/detail/{id}")
    public String qnaDetailPage(@PathVariable String id, Model model) {

        QnaResponseDto dto = qnaService.getDetail(id);
        model.addAttribute("qna", dto);

        return "admin/AdminQnaDetail";
    }

    /** ---------------------------------------------
     * 관리자 답변 저장 또는 수정
     * --------------------------------------------- */
    @PostMapping("/api/answer")
    @ResponseBody
    public ResponseEntity<QnaResponseDto> saveAnswer(@RequestBody QnaAnswerDto dto) {

        log.info("[관리자 답변 저장/수정] {}", dto);

        return ResponseEntity.ok(qnaService.saveAnswer(dto));
    }

    /** ---------------------------------------------
     * 관리자 QnA 삭제
     * --------------------------------------------- */
    @DeleteMapping("/api/{id}")
    @ResponseBody
    public ResponseEntity<String> delete(@PathVariable String id) {

        boolean ok = adminQnaService.deleteQna(id);

        if (ok) return ResponseEntity.ok("삭제되었습니다.");
        return ResponseEntity.badRequest().body("삭제할 데이터가 없습니다.");
    }
}
