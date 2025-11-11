package com.example.demo.Controller;

import com.example.demo.Domain.Common.Dto.FuneralReserveDto;
import com.example.demo.Domain.Common.Dto.GoodsReserveDto;
import com.example.demo.Domain.Common.Dto.PsyReserveDto;
import com.example.demo.Domain.Common.Dto.SignupDto;
import com.example.demo.Domain.Common.Entity.FuneralReserve;
import com.example.demo.Domain.Common.Entity.Signup;
import com.example.demo.Domain.Common.Service.FuneralReserveService;
import com.example.demo.Domain.Common.Service.GoodsReserveService;
import com.example.demo.Domain.Common.Service.PsyReserveService;
import com.example.demo.Domain.Common.Service.SignupService;
import com.example.demo.Repository.SignupRepository;
import jakarta.servlet.http.HttpSession;
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
    private final GoodsReserveService goodsReserveService;
    private final SignupService signupService;
    private final SignupRepository signupRepository;

    /* =========================================================
     *  [1] 심리상담 예약 페이지 (신규 / 수정 모드)
     *  - ?id=123 있으면 수정모드
     *  - 없으면 신규모드
     * ========================================================= */
    @GetMapping("/psy_reserve")
    public String psyReserveForm(@RequestParam(required = false) Long id,
                                 Model model,
                                 HttpSession session) {
        String userSeq = (String) session.getAttribute("userSeq");
        if (id != null) {
            PsyReserveDto dto = psyReserveService.findById(id);
            if (dto == null) {
                log.warn("[예약 수정 페이지] 존재하지 않는 ID={}", id);
                return "redirect:/mypage/Mypage";
            }
            model.addAttribute("reserve", dto);
            model.addAttribute("mode", "edit");
            log.info("[예약 수정 페이지 진입] ID={}, userSeq={}", dto.getId(), dto.getUserSeq());
        } else {
            model.addAttribute("reserve", null);
            model.addAttribute("mode", "create");
        }

        model.addAttribute("sessionUserSeq", userSeq);
        return "reserve/psy_reserve";
    }

    /* =========================================================
     *  [2] 심리상담 예약 상세조회 (fetch용)
     * ========================================================= */
    @GetMapping("/api/psy_reserve/{id}")
    @ResponseBody
    public ResponseEntity<?> getPsyReserve(@PathVariable Long id) {
        try {
            PsyReserveDto dto = psyReserveService.findById(id);
            if (dto == null) return ResponseEntity.notFound().build();
            return ResponseEntity.ok(dto);
        } catch (Exception e) {
            log.error("[상담예약 상세조회 실패]", e);
            return ResponseEntity.internalServerError().body("예약 정보를 불러올 수 없습니다.");
        }
    }

    /* =========================================================
     *  [3] 신규 상담예약 저장
     * ========================================================= */
    @PostMapping("/save1")
    @ResponseBody
    public ResponseEntity<?> savePsyReserve(
            @SessionAttribute(value = "loginUser", required = false) Object loginUser,
            @RequestBody PsyReserveDto dto) {

        try {
            if (loginUser == null)
                return ResponseEntity.status(401).body("로그인이 필요합니다.");

            String userSeq = extractUserSeq(loginUser);
            dto.setUserSeq(userSeq); // FK 연결

            PsyReserveDto saved = psyReserveService.saveReservation(dto);
            log.info("[예약 등록 완료] userSeq={}, 예약ID={}", userSeq, saved.getId());

            return ResponseEntity.ok(saved.getId());
        } catch (IllegalStateException e) {
            log.warn("[예약 저장 실패] {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            log.error("상담 예약 저장 실패", e);
            return ResponseEntity.internalServerError().body("예약 저장 실패");
        }
    }

    /* =========================================================
     *  [4] 상담예약 수정
     * ========================================================= */
    @PutMapping("/psy_reserve/{id}")
    @ResponseBody
    public ResponseEntity<?> updatePsyReserve(
            @PathVariable Long id,
            @SessionAttribute(value = "loginUser", required = false) Object loginUser,
            @RequestBody PsyReserveDto dto) {

        try {
            if (loginUser == null)
                return ResponseEntity.status(401).body("로그인이 필요합니다.");

            String userSeq = extractUserSeq(loginUser);
            PsyReserveDto updated = psyReserveService.updateReserve(id, dto, userSeq);

            log.info("[예약 수정 완료] ID={}, userSeq={}", id, userSeq);
            return ResponseEntity.ok(updated);
        } catch (IllegalStateException e) {
            log.warn("[예약 수정 실패] {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            log.error("상담 예약 수정 중 오류", e);
            return ResponseEntity.internalServerError().body("예약 수정 실패");
        }
    }

    /* =========================================================
     *  [5] 상담예약 삭제
     * ========================================================= */
    @DeleteMapping("/psy_reserve/{id}")
    @ResponseBody
    public ResponseEntity<?> deletePsyReserve(
            @PathVariable Long id,
            @SessionAttribute(value = "loginUser", required = false) Object loginUser) {

        try {
            if (loginUser == null)
                return ResponseEntity.status(401).body("로그인이 필요합니다.");

            String userSeq = extractUserSeq(loginUser);
            psyReserveService.deleteReserve(id, userSeq);

            log.info("[예약 삭제 완료] ID={}, userSeq={}", id, userSeq);
            return ResponseEntity.ok("삭제 완료");
        } catch (Exception e) {
            log.error("예약 삭제 실패", e);
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /* =========================================================
     *  [6] 날짜별 예약된 시간 조회 (중복 방지)
     * ========================================================= */
    @GetMapping("/booked-times")
    @ResponseBody
    public ResponseEntity<List<String>> getBookedTimes(
            @RequestParam String date,
            @RequestParam(required = false) Long excludeId) {

        try {
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
                return ResponseEntity.ok(psyReserveService.getBookedTimesByDate(date));
            }
        } catch (Exception e) {
            log.error("예약 시간 조회 실패", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /* =========================================================
     *  [1] 굿즈 예약 페이지 (신규 / 수정 모드)
     *  - ?id=123 있으면 수정모드
     *  - 없으면 신규모드
     * ========================================================= */
    @GetMapping("/goods_reserve")
    public String goodsReserveForm(@RequestParam(required = false) Long id,
                                 Model model,
                                 HttpSession session) {
        String userSeq = (String) session.getAttribute("userSeq");
        if (id != null) {
            GoodsReserveDto dto = goodsReserveService.findById(id);
            if (dto == null) {
                log.warn("[예약 수정 페이지] 존재하지 않는 ID={}", id);
                return "redirect:/mypage/Mypage";
            }
            model.addAttribute("reserve", dto);
            model.addAttribute("mode", "edit");
            log.info("[예약 수정 페이지 진입] ID={}, userSeq={}", dto.getId(), dto.getUserSeq());
        } else {
            model.addAttribute("reserve", null);
            model.addAttribute("mode", "create");
        }

        model.addAttribute("sessionUserSeq", userSeq);
        return "reserve/Goods_reserve";
    }

    /* =========================================================
     *  [2] 굿즈 상담 예약 상세조회 (fetch용)
     * ========================================================= */
    @GetMapping("/api/goods_reserve/{id}")
    @ResponseBody
    public ResponseEntity<?> getGoodsReserve(@PathVariable Long id) {
        try {
            GoodsReserveDto dto = goodsReserveService.findById(id);
            if (dto == null) return ResponseEntity.notFound().build();
            return ResponseEntity.ok(dto);
        } catch (Exception e) {
            log.error("[굿즈 예약 상세조회 실패]", e);
            return ResponseEntity.internalServerError().body("예약 정보를 불러올 수 없습니다.");
        }
    }

    /* =========================================================
     *  [3] 신규 굿즈 예약 저장
     * ========================================================= */
    @PostMapping("/save2")
    @ResponseBody
    public ResponseEntity<?> saveGoodsReserve(
            @SessionAttribute(value = "loginUser", required = false) Object loginUser,
            @RequestBody GoodsReserveDto dto) {

        try {
            if (loginUser == null)
                return ResponseEntity.status(401).body("로그인이 필요합니다.");

            String userSeq = extractUserSeq(loginUser);
            dto.setUserSeq(userSeq); // FK 연결

            GoodsReserveDto saved = goodsReserveService.saveReservation(dto);
            log.info("[예약 등록 완료] userSeq={}, 예약ID={}", userSeq, saved.getId());

            return ResponseEntity.ok(saved.getId());
        } catch (IllegalStateException e) {
            log.warn("[예약 저장 실패] {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            log.error("굿즈 예약 저장 실패", e);
            return ResponseEntity.internalServerError().body("예약 저장 실패");
        }
    }

    /* =========================================================
     *  [4] 굿즈 예약 수정
     * ========================================================= */
    @PutMapping("/goods_reserve/{id}")
    @ResponseBody
    public ResponseEntity<?> updateGoodsReserve(
            @PathVariable Long id,
            @SessionAttribute(value = "loginUser", required = false) Object loginUser,
            @RequestBody GoodsReserveDto dto) {

        try {
            if (loginUser == null)
                return ResponseEntity.status(401).body("로그인이 필요합니다.");

            String userSeq = extractUserSeq(loginUser);
            GoodsReserveDto updated = goodsReserveService.updateReserve(id, dto, userSeq);

            log.info("[예약 수정 완료] ID={}, userSeq={}", id, userSeq);
            return ResponseEntity.ok(updated);
        } catch (IllegalStateException e) {
            log.warn("[예약 수정 실패] {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            log.error("굿즈 예약 수정 중 오류", e);
            return ResponseEntity.internalServerError().body("예약 수정 실패");
        }
    }
        /* =========================================================
         *  [5] 굿즈 예약 삭제
         * ========================================================= */
        @DeleteMapping("/goods_reserve/{id}")
        @ResponseBody
        public ResponseEntity<?> deleteGoodsReserve(
                @PathVariable Long id,
                @SessionAttribute(value = "loginUser", required = false) Object loginUser) {

            try {
                if (loginUser == null)
                    return ResponseEntity.status(401).body("로그인이 필요합니다.");

                String userSeq = extractUserSeq(loginUser);
               goodsReserveService.deleteReserve(id, userSeq);

                log.info("[예약 삭제 완료] ID={}, userSeq={}", id, userSeq);
                return ResponseEntity.ok("삭제 완료");
            } catch (Exception e) {
                log.error("예약 삭제 실패", e);
                return ResponseEntity.badRequest().body(e.getMessage());
            }
        }





    /* =========================================================
     *  [공통] 로그인 사용자 user_seq 추출
     * ========================================================= */
    private String extractUserSeq(Object loginUser) {
        if (loginUser instanceof Signup signup) {
            return signup.getUserSeq(); // 자체 로그인
        } else if (loginUser instanceof SignupDto dto) {
            Signup user = signupRepository.findByProviderAndProviderId(dto.getProvider(), dto.getProviderId())
                    .orElseThrow(() -> new IllegalStateException("소셜 로그인 회원을 찾을 수 없습니다."));
            return user.getUserSeq(); // 소셜 로그인
        }
        throw new IllegalArgumentException("유효하지 않은 로그인 세션입니다.");
    }
}
