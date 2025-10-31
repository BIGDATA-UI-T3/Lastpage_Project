package com.example.demo.Controller;

import com.example.demo.Domain.Common.Dto.FuneralReserveDto;
import com.example.demo.Domain.Common.Dto.PsyReserveDto;
import com.example.demo.Domain.Common.Entity.FuneralReserve;
import com.example.demo.Domain.Common.Service.FuneralReserveService;
import com.example.demo.Domain.Common.Service.PsyReserveService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@Slf4j
@RequestMapping("/reserve")
@RequiredArgsConstructor
public class ReserveController {

    private final PsyReserveService psyReserveService;
    private final FuneralReserveService funeralReserveService;

    // 페이지 이동
    @GetMapping("/psy_reserve")
    public String psy_reservePage(Model model) {
        model.addAttribute("mode", "create");
        return "reserve/psy_reserve";
    }

    @GetMapping("/Funeral_reserve")
    public String funeral_reservePage() {
        return "reserve/Funeral_reserve";
    }

    //  상담예약 저장 (최초)
    @PostMapping("/save1")
    @ResponseBody
    public ResponseEntity<?> savePsyReserve(@RequestBody PsyReserveDto dto) {
        try {
            PsyReserveDto saved = psyReserveService.saveReservation(dto);
            return ResponseEntity.ok(saved.getEmail());
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            log.error("상담 예약 저장 실패", e);
            return ResponseEntity.internalServerError().body("예약 저장 실패");
        }
    }

    /** ------------------------------
     *  상담예약 수정 페이지 이동 (edit 모드)
     * ------------------------------ */
    @GetMapping("/psy_reserve/edit/{id}")
    public String editPsyReserve(@PathVariable Long id, Model model) {
        PsyReserveDto dto = psyReserveService.findById(id);
        if (dto == null) {
            log.warn("[수정 페이지 진입 실패] 존재하지 않는 ID={}", id);
            return "redirect:/mypage/Mypage";
        }
        model.addAttribute("reserve", dto);
        model.addAttribute("mode", "edit");  // HTML 내에서 edit모드 분기 가능
        log.info("[수정 페이지 진입] ID={}, Email={}", dto.getId(), dto.getEmail());
        return "reserve/psy_reserve";  // 기존 예약 폼 그대로 사용
    }


    /** ------------------------------
     *  상담예약 수정 (PUT 요청)
     * ------------------------------ */
    @PutMapping("/psy_reserve/{id}")
    @ResponseBody
    public ResponseEntity<?> updatePsyReserve(@PathVariable Long id,
                                              @RequestBody PsyReserveDto dto) {
        try {
            PsyReserveDto updated = psyReserveService.updateReserve(id, dto);
            return ResponseEntity.ok(updated);
        } catch (IllegalStateException e) {
            log.warn("[예약 수정 실패] {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            log.error("[상담 예약 수정 중 오류]", e);
            return ResponseEntity.internalServerError().body("예약 수정 실패");
        }
    }

    //  상담예약 삭제
    @DeleteMapping("/psy_reserve/{id}")
    @ResponseBody
    public ResponseEntity<?> deletePsyReserve(@PathVariable Long id) {
        try {
            psyReserveService.deleteReserve(id);
            return ResponseEntity.ok("삭제 완료");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    //  날짜별 예약된 시간 조회 (JS에서 시간 비활성화용)
    @GetMapping("/booked-times")
    @ResponseBody
    public ResponseEntity<List<String>> getBookedTimes(@RequestParam String date,
                                                       @RequestParam(required = false) Long excludeId) {
        try {
            // 수정 모드: 내 예약은 제외
            if (excludeId != null) {
                PsyReserveDto mine = psyReserveService.findById(excludeId);
                List<String> all = psyReserveService.getBookedTimesByDate(date);

                if (mine != null && date.equals(mine.getConsultDate())) {
                    all = all.stream()
                            .filter(t -> !t.equals(mine.getTime()))
                            .toList();
                }
                return ResponseEntity.ok(all);
            } else {
                // 신규 예약
                return ResponseEntity.ok(psyReserveService.getBookedTimesByDate(date));
            }
        } catch (Exception e) {
            log.error("예약 시간 조회 실패", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    // 장례 예약 저장
    @PostMapping("/save2")
    @ResponseBody
    public ResponseEntity<?> saveFuneralReserve(@RequestBody FuneralReserveDto dto) {
        try {
            FuneralReserve saved = funeralReserveService.saveReservation(dto);
            return ResponseEntity.ok(saved.getOwnerName());
        } catch (Exception e) {
            log.error("장례 예약 저장 실패", e);
            return ResponseEntity.internalServerError().body("예약 저장 실패");
        }
    }
}
