package com.example.demo.Controller;

import com.example.demo.Domain.Common.Dto.FuneralReserveDto;
import com.example.demo.Domain.Common.Dto.PsyReserveDto;
import com.example.demo.Domain.Common.Entity.FuneralReserve;
import com.example.demo.Domain.Common.Entity.PsyReserve;
import com.example.demo.Domain.Common.Service.FuneralReserveService;
import com.example.demo.Domain.Common.Service.PsyReserveService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Controller
@RequestMapping("/reserve")
@RequiredArgsConstructor
public class ReserveController {

    private final PsyReserveService psyReserveService;
    private final FuneralReserveService funeralReserveService;

    // ======================== [페이지 이동] ======================== //
    @GetMapping("/psy_reserve")
    public String psy_reservePage(@RequestParam(required = false) Long id, org.springframework.ui.Model model) {
        if (id != null) {
            PsyReserveDto reserve = psyReserveService.findById(id);
            model.addAttribute("reserve", reserve); // 수정용 데이터 전달
        }
        return "reserve/psy_reserve";
    }

//    @GetMapping("/Funeral_reserve")
//    public String funeral_reservePage(@RequestParam(required = false) Long id, org.springframework.ui.Model model) {
//        if (id != null) {
//            FuneralReserve reserve = funeralReserveService.findByOwerName(ownerName);
//            model.addAttribute("reserve", reserve);
//        }
//        return "reserve/Funeral_reserve";
//    }

    // ======================== [심리상담 예약 저장] ======================== //
    @PostMapping("/save1")
    @ResponseBody
    public ResponseEntity<?> savePsyReserve(@RequestBody PsyReserveDto dto) {
        try {
            // 날짜 중복 체크
            if (psyReserveService.isDateReserved(dto.getConsultDate(), dto.getTime())) {
                return ResponseEntity.badRequest().body("해당 날짜와 시간은 이미 예약되어 있습니다.");
            }

            PsyReserveDto saved = psyReserveService.saveReservation(dto);
            log.info("[심리상담 예약 저장 완료] 이메일={}", saved.getEmail());
            return ResponseEntity.ok(saved.getEmail());
        } catch (Exception e) {
            log.error("[심리상담 예약 저장 실패]", e);
            return ResponseEntity.internalServerError().body("예약 저장 실패");
        }
    }

    // ======================== [심리상담 예약 수정] ======================== //
    @PutMapping("/psy_reserve/{id}")
    @ResponseBody
    public ResponseEntity<?> updatePsyReserve(@PathVariable Long id, @RequestBody PsyReserveDto dto) {
        try {
            if (psyReserveService.isDateReserved(dto.getConsultDate(), dto.getTime(), id)) {
                return ResponseEntity.badRequest().body("해당 날짜와 시간은 이미 예약되어 있습니다.");
            }

            psyReserveService.updateReserve(id, dto);
            log.info("[심리상담 예약 수정 완료] ID={}", id);
            return ResponseEntity.ok("상담 예약이 수정되었습니다.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            log.error("[심리상담 예약 수정 실패]", e);
            return ResponseEntity.internalServerError().body("예약 수정 실패");
        }
    }

    // ======================== [심리상담 예약 삭제] ======================== //
    @DeleteMapping("/psy_reserve/{id}")
    @ResponseBody
    public ResponseEntity<?> deletePsyReserve(@PathVariable Long id) {
        try {
            boolean deleted = psyReserveService.deleteReserve(id);
            if (!deleted) {
                return ResponseEntity.badRequest().body("해당 예약이 존재하지 않습니다.");
            }
            log.info("[심리상담 예약 삭제 완료] ID={}", id);
            return ResponseEntity.ok("예약이 삭제되었습니다.");
        } catch (Exception e) {
            log.error("[심리상담 예약 삭제 실패]", e);
            return ResponseEntity.internalServerError().body("예약 삭제 중 오류 발생");
        }
    }

    // ======================== [심리상담 중복 체크 API] ======================== //
    @GetMapping("/psy_reserve/check-duplicate")
    @ResponseBody
    public ResponseEntity<?> checkPsyDuplicate(
            @RequestParam String consultDate,
            @RequestParam String time,
            @RequestParam(required = false) Long id) {

        boolean reserved = (id == null)
                ? psyReserveService.isDateReserved(consultDate, time)
                : psyReserveService.isDateReserved(consultDate, time, id);

        return ResponseEntity.ok(reserved);
    }

    // ======================== [장례 예약 저장] ======================== //
//    @PostMapping("/save2")
//    @ResponseBody
//    public ResponseEntity<?> saveFuneralReserve(@RequestBody FuneralReserveDto dto) {
//        try {
//            if (funeralReserveService.isDateReserved(dto.getFuneralDate(), dto.getTime())) {
//                return ResponseEntity.badRequest().body("해당 날짜와 시간은 이미 예약되어 있습니다.");
//            }
//
//            FuneralReserve saved = funeralReserveService.saveReservation(dto);
//            log.info("[장례 예약 저장 완료] 예약자={}", saved.getOwnerName());
//            return ResponseEntity.ok(saved.getOwnerName());
//        } catch (Exception e) {
//            log.error("[장례 예약 저장 실패]", e);
//            return ResponseEntity.internalServerError().body("예약 저장 실패");
//        }
//    }

//    // ======================== [장례 예약 수정] ======================== //
//    @PutMapping("/Funeral_reserve/{id}")
//    @ResponseBody
//    public ResponseEntity<?> updateFuneralReserve(@PathVariable Long id, @RequestBody FuneralReserveDto dto) {
//        try {
//            if (funeralReserveService.isDateReserved(dto.getFuneralDate(), dto.getTime(), id)) {
//                return ResponseEntity.badRequest().body("해당 날짜와 시간은 이미 예약되어 있습니다.");
//            }
//
//            funeralReserveService.updateReserve(id, dto);
//            log.info("[장례 예약 수정 완료] ID={}", id);
//            return ResponseEntity.ok("장례 예약이 수정되었습니다.");
//        } catch (IllegalArgumentException e) {
//            return ResponseEntity.badRequest().body(e.getMessage());
//        } catch (Exception e) {
//            log.error("[장례 예약 수정 실패]", e);
//            return ResponseEntity.internalServerError().body("예약 수정 실패");
//        }
//    }
//
//    // ======================== [장례 예약 삭제] ======================== //
//    @DeleteMapping("/Funeral_reserve/{id}")
//    @ResponseBody
//    public ResponseEntity<?> deleteFuneralReserve(@PathVariable Long id) {
//        try {
//            boolean deleted = funeralReserveService.deleteReserve(id);
//            if (!deleted) {
//                return ResponseEntity.badRequest().body("해당 예약이 존재하지 않습니다.");
//            }
//            log.info("[장례 예약 삭제 완료] ID={}", id);
//            return ResponseEntity.ok("예약이 삭제되었습니다.");
//        } catch (Exception e) {
//            log.error("[장례 예약 삭제 실패]", e);
//            return ResponseEntity.internalServerError().body("예약 삭제 중 오류 발생");
//        }
//    }
//
//    // ======================== [장례 예약 중복 체크 API] ======================== //
//    @GetMapping("/Funeral_reserve/check-duplicate")
//    @ResponseBody
//    public ResponseEntity<?> checkFuneralDuplicate(
//            @RequestParam String funeralDate,
//            @RequestParam String time,
//            @RequestParam(required = false) Long id) {
//
//        boolean reserved = (id == null)
//                ? funeralReserveService.isDateReserved(funeralDate, time)
//                : funeralReserveService.isDateReserved(funeralDate, time, id);
//
//        return ResponseEntity.ok(reserved);
//    }
}
