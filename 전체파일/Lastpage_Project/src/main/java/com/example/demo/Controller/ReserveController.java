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

@Controller
@Slf4j
@RequestMapping("/reserve")
@RequiredArgsConstructor
public class ReserveController {

    private final PsyReserveService psyReserveService;
    private final FuneralReserveService funeralReserveService;

    // ======================== [페이지 이동] ======================== //
    @GetMapping("/psy_reserve")
    public String psy_reservePage() {
        return "reserve/psy_reserve";
    }

    @GetMapping("/Funeral_reserve")
    public String funeral_reservePage() {
        return "reserve/Funeral_reserve";
    }

    // ======================== [심리상담 예약 저장] ======================== //
    @PostMapping("/save1")
    @ResponseBody
    public ResponseEntity<?> savePsyReserve(@RequestBody PsyReserveDto dto) {
        try {
            PsyReserve saved = psyReserveService.saveReservation(dto);
            log.info("✅ 예약 저장 완료: {}", saved.getEmail());

            // JS에서 redirect할 수 있도록 email을 응답에 담아줌
            return ResponseEntity.ok(saved.getEmail());
        } catch (Exception e) {
            log.error("❌ 예약 저장 실패", e);
            return ResponseEntity.internalServerError().body("예약 저장 실패");
        }
    }

    // ======================== [장례 예약 저장] ======================== //
    @PostMapping("/save2")
    @ResponseBody
    public ResponseEntity<?> saveFuneralReserve(@RequestBody FuneralReserveDto dto) {
        try {
            FuneralReserve saved = funeralReserveService.saveReservation(dto);
            log.info("✅ 장례 예약 저장 완료: {}", saved.getOwnerName());
            return ResponseEntity.ok(saved.getOwnerName());
        } catch (Exception e) {
            log.error("❌ 장례 예약 저장 실패", e);
            return ResponseEntity.internalServerError().body("예약 저장 실패");
        }
    }
}
