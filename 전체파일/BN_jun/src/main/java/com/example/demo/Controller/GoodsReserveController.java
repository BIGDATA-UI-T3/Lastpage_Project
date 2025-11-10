package com.example.demo.Controller;

import com.example.demo.Domain.Common.Dto.GoodsReserveDto;
import com.example.demo.Domain.Common.Entity.GoodsReserve;
import com.example.demo.Domain.Common.Service.GoodsReserveService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j; // [추가]
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

@Controller
@Slf4j // [추가]
@RequiredArgsConstructor
@RequestMapping("/goods-reserve") // [수정] 기본 경로를 하나로 통합
public class GoodsReserveController {

    private final GoodsReserveService goodsReserveService;

    // --- 1. 생성(Create) 기능 (기존 ReserveController에서 가져옴) ---

    /**
     * 굿즈 예약 페이지 (새 예약 양식)
     */
    @GetMapping("/new")
    public String reservePage() {
        // "existingData" 모델 속성이 없으므로, 템플릿은 새 양식으로 렌더링됩니다.
        return "reserve/Goods_reserve";
    }

    /**
     * 굿즈 예약 저장 (새 예약)
     */
    @PostMapping("/save")
    @ResponseBody
    public ResponseEntity<?> saveReserve(@RequestBody GoodsReserveDto dto, Principal principal) {

        if (principal == null) {
            log.warn("로그인하지 않은 사용자가 예약을 시도했습니다.");
            return ResponseEntity.status(401).body("로그인이 필요합니다.");
        }

        try {
            String username = principal.getName();
            GoodsReserve saved = goodsReserveService.saveReservation(dto, username);

            log.info(" 예약 저장 완료: {}", saved.getOwnerName());
            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            log.error(" 예약 저장 실패", e);
            return ResponseEntity.internalServerError().body("예약 저장 실패: " + e.getMessage());
        }
    }

    // --- 2. 수정(Update) 및 읽기(Read) 기능 (기존 GoodsReserveController) ---

    /**
     * 굿즈 예약 수정 페이지 (기존 데이터가 채워진 양식)
     */
    @GetMapping("/edit/{id}")
    public String showEditPage(@PathVariable("id") Long id, Model model, Principal principal) {

        GoodsReserve existingData = goodsReserveService.getReservationById(id);

        if (principal == null || !existingData.getUser().getUsername().equals(principal.getName())) {
            // TODO: 권한 없음 오류 페이지
            return "redirect:/mypage";
        }

        // "existingData"를 모델에 추가합니다.
        // 템플릿(Goods_reserve.html)은 이 데이터가 있으면 '수정 모드'로 렌더링됩니다.
        model.addAttribute("existingData", existingData);

        return "reserve/Goods_reserve"; // 👈 생성(new)과 같은 템플릿 파일 사용
    }

    /**
     * 굿즈 예약 수정 (업데이트)
     */
    @PostMapping("/update/{id}")
    @ResponseBody
    public ResponseEntity<?> updateReservation(
            @PathVariable("id") Long id,
            @RequestBody GoodsReserveDto dto,
            Principal principal) {

        if (principal == null) {
            return ResponseEntity.status(401).body("로그인이 필요합니다.");
        }

        try {
            String username = principal.getName();
            GoodsReserve updated = goodsReserveService.updateReservation(id, dto, username);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("예약 수정 실패: " + e.getMessage());
        }
    }

    // --- 3. 삭제(Delete) 기능 (기존 GoodsReserveController) ---

    /**
     * 굿즈 예약 삭제 (취소)
     */
    @PostMapping("/delete/{id}")
    public String deleteReservation(
            @PathVariable("id") Long id,
            RedirectAttributes redirectAttributes,
            Principal principal) { // [수정] 본인 확인을 위해 Principal 추가

        try {
            // [수정] 본인 확인 로직 추가
            String username = (principal != null) ? principal.getName() : null;
            goodsReserveService.deleteReservation(id, username); // 👈 username 넘겨서 확인

            redirectAttributes.addFlashAttribute("successMessage", "예약이 성공적으로 취소되었습니다.");

        } catch (Exception e) { // (e.g., AccessDeniedException 또는 RuntimeException)
            redirectAttributes.addFlashAttribute("errorMessage", "예약 취소 중 오류가 발생했습니다: " + e.getMessage());
        }

        return "redirect:/mypage";
    }
}