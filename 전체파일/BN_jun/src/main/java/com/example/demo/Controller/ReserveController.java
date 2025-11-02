package com.example.demo.Controller;

import com.example.demo.Domain.Common.Dto.ReserveDto;
import com.example.demo.Domain.Common.Entity.GoodsReserve;
import com.example.demo.Domain.Common.Service.GoodsReserveService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication; // [추가]
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.security.Principal; // [추가]

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
    // [수정] Principal 객체를 파라미터로 받아 현재 로그인한 사용자 정보를 가져옵니다.
    public ResponseEntity<?> saveReserve(@RequestBody ReserveDto dto, Principal principal) {

        // [추가] 비로그인 사용자가 예약을 시도하는 경우 차단 (이중 방어)
        if (principal == null) {
            log.warn("로그인하지 않은 사용자가 예약을 시도했습니다.");
            return ResponseEntity.status(401).body("로그인이 필요합니다.");
        }

        try {
            // [수정] 현재 로그인한 사용자의 username을 서비스로 전달
            String username = principal.getName();
            GoodsReserve saved = goodsReserveService.saveReservation(dto, username);

            log.info(" 예약 저장 완료: {}", saved.getOwnerName());
            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            log.error(" 예약 저장 실패", e);
            return ResponseEntity.internalServerError().body("예약 저장 실패: " + e.getMessage());
        }
    }
}