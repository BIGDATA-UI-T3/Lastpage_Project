package com.example.demo.Controller;

import com.example.demo.Domain.Common.Dto.ReserveDto;
import com.example.demo.Domain.Common.Entity.PsyReserve;
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

    // 페이지 이동 (GET 요청)
    @GetMapping("/psy_reserve")
    public String reservePage() {
        return "reserve/psy_reserve";  // templates/reserve/psy_reserve.html
    }

    // 예약 저장 (AJAX POST)
    @PostMapping("/save")
    @ResponseBody
    public ResponseEntity<?> saveReserve(@RequestBody ReserveDto dto) {
        try {
            PsyReserve saved = psyReserveService.saveReservation(dto);
            log.info(" 예약 저장 완료: {}", saved.getName());
            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            log.error(" 예약 저장 실패", e);
            return ResponseEntity.internalServerError().body("예약 저장 실패");
        }
    }
}
