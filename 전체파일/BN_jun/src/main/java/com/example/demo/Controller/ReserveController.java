package com.example.demo.Controller;

import com.example.demo.Domain.Common.Dto.ReserveDto;
import com.example.demo.Domain.Common.Entity.GoodsReserve;
import com.example.demo.Domain.Common.Service.GoodsReserveService;
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

    private final GoodsReserveService goodsReserveService;

    @GetMapping("/Goods_reserve")
    public String reservePage() {
        return "reserve/Goods_reserve";
    }

    @PostMapping("/save")
    @ResponseBody
    public ResponseEntity<?> saveReserve(@RequestBody ReserveDto dto) {
        try {
            GoodsReserve saved = goodsReserveService.saveReservation(dto);
            log.info(" 예약 저장 완료: {}", saved.getOwnerName());
            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            log.error(" 예약 저장 실패", e);
            return ResponseEntity.internalServerError().body("예약 저장 실패");
        }
    }
}
