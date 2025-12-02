package com.example.demo.Controller;

import com.example.demo.Domain.Common.Dto.*;
import com.example.demo.Domain.Common.Entity.FuneralReserve;
import com.example.demo.Domain.Common.Entity.Signup;
import com.example.demo.Domain.Common.Service.*;
import com.example.demo.Repository.OurpageReserveRepository;
import com.example.demo.Repository.SignupRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Controller
@Slf4j
@RequestMapping("/reserve")
@RequiredArgsConstructor
@Transactional
public class ReserveController {

    private final PsyReserveService psyReserveService;
    private final FuneralReserveService funeralReserveService;
    private final GoodsReserveService goodsReserveService;
    private final OurpageReserveService ourpageReserveService;
    private final SignupService signupService;
    private final SignupRepository signupRepository;
    private final OurpageReserveRepository ourpageReserveRepository;

    /* =========================================================
     *   심리상담 예약 컨트롤러 (관리자 / 사용자 통합)
     * ========================================================= */

    @GetMapping({"/psy_reserve", "/psy_reserve/{id}",
            "/admin/reserve/psy_reserve", "/admin/reserve/psy_reserve/{id}"})
    public String psyReserveForm(@RequestParam(required = false) Long id,
                                 @RequestParam(required = false) String targetUserSeq,
                                 Model model,
                                 HttpServletRequest request,
                                 @SessionAttribute(value = "loginUser", required = false) Object loginUser) {

        if (loginUser == null) {
            log.warn("[심리예약 페이지 접근] 세션 없음 → 로그인 필요");
            return "redirect:/signin";
        }

        // 1) 관리자 요청 여부
        String uri = request.getRequestURI();
        boolean isAdminRequest = uri.startsWith("/reserve/admin/");

        // 2) 로그인한 세션 사용자
        String sessionUserSeq = extractUserSeq(loginUser);

        // 3) 실제 조회 기준이 되는 userSeq
        String actualUserSeq = isAdminRequest
                ? (targetUserSeq != null ? targetUserSeq : sessionUserSeq)
                : sessionUserSeq;

        // 4) 수정 모드이면 데이터 불러오기
        PsyReserveDto dto = null;

        if (id != null) {
            dto = psyReserveService.findByIdForAdminOrUser(id, actualUserSeq, isAdminRequest);
            if (dto == null) {
                log.warn("[심리예약 수정 페이지] 존재하지 않는 ID={}", id);
                return "redirect:/mypage/Mypage";
            }
            model.addAttribute("mode", isAdminRequest ? "admin-edit" : "edit");
        } else {
            model.addAttribute("mode", isAdminRequest ? "admin-create" : "create");
        }

        model.addAttribute("reserve", dto);
        model.addAttribute("sessionUserSeq", sessionUserSeq);
        model.addAttribute("targetUserSeq", actualUserSeq);

        log.info("[{}] 심리예약 페이지 진입 → sessionUserSeq={}, targetUserSeq={}, id={}",
                isAdminRequest ? "ADMIN" : "USER",
                sessionUserSeq, actualUserSeq, id);

        return "reserve/psy_reserve";
    }


    /* =========================================================
     *   [2] 상세 조회 (fetch용)
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
     *   [3] 신규 저장 (공용)
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
            dto.setUserSeq(userSeq);

            PsyReserveDto saved = psyReserveService.saveReservation(dto);

            log.info("[상담예약 등록 완료] userSeq={}, 예약ID={}", userSeq, saved.getId());
            return ResponseEntity.ok(saved.getId());

        } catch (Exception e) {
            log.error("상담 예약 저장 실패", e);
            return ResponseEntity.internalServerError().body("예약 저장 실패");
        }
    }


    /* =========================================================
     *   [4] 수정 (관리자 / 사용자 통합)
     * ========================================================= */
    @PutMapping({"/psy_reserve/{id}", "/admin/reserve/psy_reserve/{id}"})
    @ResponseBody
    public ResponseEntity<?> updatePsyReserve(
            @PathVariable Long id,
            @RequestParam(required = false) String targetUserSeq,
            HttpServletRequest request,
            @SessionAttribute(value = "loginUser", required = false) Object loginUser,
            @RequestBody PsyReserveDto dto) {

        try {
            if (loginUser == null)
                return ResponseEntity.status(401).body("로그인이 필요합니다.");

            String uri = request.getRequestURI();
            boolean isAdminRequest = uri.startsWith("/reserve/admin/");
            String sessionUserSeq = extractUserSeq(loginUser);

            String actualUserSeq = isAdminRequest
                    ? (targetUserSeq != null ? targetUserSeq : sessionUserSeq)
                    : sessionUserSeq;

            PsyReserveDto updated =
                    psyReserveService.updateForAdminOrUser(id, dto, actualUserSeq, isAdminRequest);

            log.info("[{} 상담예약 수정] ID={}, 수행자={}, 대상UserSeq={}",
                    isAdminRequest ? "ADMIN" : "USER",
                    id, sessionUserSeq, actualUserSeq);

            return ResponseEntity.ok(updated);

        } catch (Exception e) {
            log.error("상담 예약 수정 중 오류", e);
            return ResponseEntity.internalServerError().body("예약 수정 실패");
        }
    }


    /* =========================================================
     *   [5] 삭제 (관리자 / 사용자 통합)
     * ========================================================= */
    @DeleteMapping({"/psy_reserve/{id}", "/admin/reserve/psy_reserve/{id}"})
    @ResponseBody
    public ResponseEntity<?> deletePsyReserve(
            @PathVariable Long id,
            @RequestParam(required = false) String targetUserSeq,
            HttpServletRequest request,
            @SessionAttribute(value = "loginUser", required = false) Object loginUser) {

        try {
            if (loginUser == null)
                return ResponseEntity.status(401).body("로그인이 필요합니다.");

            String uri = request.getRequestURI();
            boolean isAdminRequest = uri.startsWith("/reserve/admin/");
            String sessionUserSeq = extractUserSeq(loginUser);

            String actualUserSeq = isAdminRequest
                    ? (targetUserSeq != null ? targetUserSeq : sessionUserSeq)
                    : sessionUserSeq;

            psyReserveService.deleteForAdminOrUser(id, actualUserSeq, isAdminRequest);

            log.info("[{} 상담예약 삭제] ID={}, 수행자={}, 대상UserSeq={}",
                    isAdminRequest ? "ADMIN" : "USER",
                    id, sessionUserSeq, actualUserSeq);

            return ResponseEntity.ok("삭제 완료");

        } catch (Exception e) {
            log.error("예약 삭제 실패", e);
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }


    /* =========================================================
     *   [6] 날짜별 예약된 시간 조회 (중복 방지)
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
     *  굿즈 예약 컨트롤러 (관리자/사용자 통합 버전)
     * ========================================================= */

    @GetMapping({"/goods_reserve", "/goods_reserve/{id}",
            "/admin/reserve/goods_reserve", "/admin/reserve/goods_reserve/{id}"})
    public String goodsReserveForm(@RequestParam(required = false) Long id,
                                   @RequestParam(required = false) String targetUserSeq,
                                   Model model,
                                   HttpServletRequest request,
                                   @SessionAttribute(value = "loginUser", required = false) Object loginUser) {

        if (loginUser == null) {
            log.warn("[굿즈예약 페이지 접근] 세션 없음 → 로그인 필요");
            return "redirect:/signin";
        }

        // 1) 관리자 요청인지 판별
        String uri = request.getRequestURI();
        boolean isAdminRequest = uri.startsWith("/reserve/admin/");

        // 2) 로그인한 사용자
        String sessionUserSeq = extractUserSeq(loginUser);

        // 3) 조회 기준 userSeq 결정
        String actualUserSeq = isAdminRequest
                ? (targetUserSeq != null ? targetUserSeq : sessionUserSeq)
                : sessionUserSeq;

        // 4) 수정 모드라면 예약 조회
        GoodsReserveDto dto = null;

        if (id != null) {

            dto = goodsReserveService.findByIdForAdminOrUser(id, actualUserSeq, isAdminRequest);

            if (dto == null) {
                log.warn("[굿즈 수정 페이지] 존재하지 않는 ID={}", id);
                return "redirect:/mypage/Mypage";
            }

            model.addAttribute("mode", isAdminRequest ? "admin-edit" : "edit");

        } else {
            model.addAttribute("mode", isAdminRequest ? "admin-create" : "create");
        }

        model.addAttribute("reserve", dto);
        model.addAttribute("sessionUserSeq", sessionUserSeq);
        model.addAttribute("targetUserSeq", actualUserSeq);

        log.info("[{}] 굿즈 예약 페이지 진입 → sessionUserSeq={}, targetUserSeq={}, id={}",
                isAdminRequest ? "ADMIN" : "USER",
                sessionUserSeq, actualUserSeq, id);

        return "reserve/Goods_reserve";
    }


    /* =========================================================
     *  [2] 굿즈 예약 상세조회 (fetch용 / 공용)
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
     *  [3] 신규 굿즈 예약 저장 (공용)
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
            dto.setUserSeq(userSeq);

            GoodsReserveDto saved = goodsReserveService.saveReservation(dto);

            log.info("[굿즈 예약 등록 완료] userSeq={}, 예약ID={}", userSeq, saved.getId());
            return ResponseEntity.ok(saved.getId());

        } catch (Exception e) {
            log.error("굿즈 예약 저장 실패", e);
            return ResponseEntity.internalServerError().body("굿즈 예약 저장 실패");
        }
    }


    /* =========================================================
     *  [4] 굿즈 예약 수정 (관리자/사용자 통합)
     * ========================================================= */
    @PutMapping({"/goods_reserve/{id}", "/admin/reserve/goods_reserve/{id}"})
    @ResponseBody
    public ResponseEntity<?> updateGoodsReserve(
            @PathVariable Long id,
            @RequestParam(required = false) String targetUserSeq,
            HttpServletRequest request,
            @SessionAttribute(value = "loginUser", required = false) Object loginUser,
            @RequestBody GoodsReserveDto dto) {

        try {
            if (loginUser == null)
                return ResponseEntity.status(401).body("로그인이 필요합니다.");

            String uri = request.getRequestURI();
            boolean isAdminRequest = uri.startsWith("/reserve/admin/");
            String sessionUserSeq = extractUserSeq(loginUser);

            String actualUserSeq = isAdminRequest
                    ? (targetUserSeq != null ? targetUserSeq : sessionUserSeq)
                    : sessionUserSeq;

            GoodsReserveDto updated =
                    goodsReserveService.updateForAdminOrUser(id, dto, actualUserSeq, isAdminRequest);

            log.info("[{} 굿즈 예약 수정 완료] ID={}, 수정요청자={}, 대상UserSeq={}",
                    isAdminRequest ? "ADMIN" : "USER",
                    id, sessionUserSeq, actualUserSeq);

            return ResponseEntity.ok(updated);

        } catch (Exception e) {
            log.error("굿즈 예약 수정 중 오류", e);
            return ResponseEntity.internalServerError().body("굿즈 예약 수정 실패");
        }
    }


    /* =========================================================
     *  [5] 굿즈 예약 삭제 (관리자/사용자 통합)
     * ========================================================= */
    @DeleteMapping({"/goods_reserve/{id}", "/admin/reserve/goods_reserve/{id}"})
    @ResponseBody
    public ResponseEntity<?> deleteGoodsReserve(
            @PathVariable Long id,
            @RequestParam(required = false) String targetUserSeq,
            HttpServletRequest request,
            @SessionAttribute(value = "loginUser", required = false) Object loginUser) {

        try {
            if (loginUser == null)
                return ResponseEntity.status(401).body("로그인이 필요합니다.");

            String uri = request.getRequestURI();
            boolean isAdminRequest = uri.startsWith("/reserve/admin/");
            String sessionUserSeq = extractUserSeq(loginUser);

            String actualUserSeq = isAdminRequest
                    ? (targetUserSeq != null ? targetUserSeq : sessionUserSeq)
                    : sessionUserSeq;

            goodsReserveService.deleteForAdminOrUser(id, actualUserSeq, isAdminRequest);

            log.info("[{} 굿즈 예약 삭제 완료] ID={}, 삭제요청자={}, 대상UserSeq={}",
                    isAdminRequest ? "ADMIN" : "USER",
                    id, sessionUserSeq, actualUserSeq);

            return ResponseEntity.ok("삭제 완료");

        } catch (Exception e) {
            log.error("굿즈 예약 삭제 실패", e);
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /* =========================================================
     *  [1] 장례 예약 페이지 (신규 / 수정 모드)
     *  - ?id=123 있으면 수정모드
     *  - 없으면 신규모드
     * ========================================================= */
    @GetMapping({"/funeral_reserve", "/funeral_reserve/{id}",
            "/admin/reserve/funeral_reserve", "/admin/reserve/funeral_reserve/{id}"})

    public String funeralReserveForm(@RequestParam(required = false) Long id,
                                     @RequestParam(required = false) String targetUserSeq,
                                     Model model,
                                     HttpServletRequest request,
                                     @SessionAttribute(value = "loginUser", required = false) Object loginUser) {

        if (loginUser == null) {
            log.warn("[장례예약 페이지 접근] 세션 없음 → 로그인 필요");
            return "redirect:/signin";
        }

        // 1) 관리자 요청인지 판별
        String uri = request.getRequestURI();
        boolean isAdminRequest = uri.startsWith("/reserve/admin/");


        // 2) 세션 사용자 (로그인한 사람)
        String sessionUserSeq = extractUserSeq(loginUser);

        // 3) 조회 기준이 되는 userSeq 결정
        String actualUserSeq = isAdminRequest
                ? (targetUserSeq != null ? targetUserSeq : sessionUserSeq)
                : sessionUserSeq;

        // 4) 예약 조회
        FuneralReserveDto dto = null;
        if (id != null) {
            dto = funeralReserveService.findByIdForAdminOrUser(id, actualUserSeq, isAdminRequest);
            if (dto == null) {
                log.warn("[예약 수정 페이지] 존재하지 않는 ID={}", id);
                return "redirect:/mypage/Mypage";
            }
            model.addAttribute("mode", isAdminRequest ? "admin-edit" : "edit");
        } else {
            model.addAttribute("mode", isAdminRequest ? "admin-create" : "create");
        }

        model.addAttribute("reserve", dto);
        model.addAttribute("sessionUserSeq", sessionUserSeq);   // 로그인한 사람
        model.addAttribute("targetUserSeq", actualUserSeq);     // 수정 대상 사용자

        log.info("[{}] 장례 예약 페이지 진입 -> sessionUserSeq={}, targetUserSeq={}, id={}",
                isAdminRequest ? "ADMIN" : "USER",
                sessionUserSeq, actualUserSeq, id);

        return "reserve/Funeral_reserve";
    }

    /* =========================================================
     *  [2] 장례 예약 상세조회 (fetch용)
     * ========================================================= */
    @GetMapping("/api/funeral_reserve/{id}")
    @ResponseBody
    public ResponseEntity<?> getFuneralReserve(@PathVariable Long id) {
        try {
            FuneralReserveDto dto = funeralReserveService.findById(id);
            if (dto == null) return ResponseEntity.notFound().build();
            return ResponseEntity.ok(dto);
        } catch (Exception e) {
            log.error("[장례 예약 상세조회 실패]", e);
            return ResponseEntity.internalServerError().body("예약 정보를 불러올 수 없습니다.");
        }
    }

    /* =========================================================
     *  [3] 신규 장례 예약 저장
     * ========================================================= */
    @PostMapping("/save3")
    @ResponseBody
    public ResponseEntity<?> saveFuneralReserve(
            @SessionAttribute(value = "loginUser", required = false) Object loginUser,
            @RequestBody FuneralReserveDto dto) {

        try {
            if (loginUser == null)
                return ResponseEntity.status(401).body("로그인이 필요합니다.");

            String userSeq = extractUserSeq(loginUser);
            dto.setUserSeq(userSeq); // FK 연결

            FuneralReserveDto saved = funeralReserveService.saveReservation(dto);
            log.info("[장례 예약 등록 완료] userSeq={}, 예약ID={}", userSeq, saved.getId());

            return ResponseEntity.ok(saved.getId());
        } catch (IllegalStateException e) {

            log.warn("[예약 저장 실패] {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {

            log.error("장례 예약 저장 실패", e);
            return ResponseEntity.internalServerError().body("예약 저장 실패");
        }
    }

    /* =========================================================
     *  [4] 장례 예약 수정
     * ========================================================= */
    @PutMapping({"/funeral_reserve/{id}", "/admin/reserve/funeral_reserve/{id}"})
    @ResponseBody
    public ResponseEntity<?> updateFuneralReserve(
            @PathVariable Long id,
            @RequestParam(required = false) String targetUserSeq,
            HttpServletRequest request,
            @SessionAttribute(value = "loginUser", required = false) Object loginUser,
            @RequestBody FuneralReserveDto dto) {


        try {
            if (loginUser == null)
                return ResponseEntity.status(401).body("로그인이 필요합니다.");




            String uri = request.getRequestURI();
            boolean isAdminRequest = uri.startsWith("/reserve/admin/");

            log.info("ADMIN DETECT? uri={}, isAdmin={}", uri, isAdminRequest);


            String sessionUserSeq = extractUserSeq(loginUser);

            String actualUserSeq = isAdminRequest
                    ? (targetUserSeq != null ? targetUserSeq : sessionUserSeq)
                    : sessionUserSeq;

            FuneralReserveDto updated =
                    funeralReserveService.updateForAdminOrUser(id, dto, actualUserSeq, isAdminRequest);

            log.info("[{} 예약 수정 완료] ID={}, 수정요청자={}, 대상UserSeq={}",
                    isAdminRequest ? "ADMIN" : "USER",
                    id, sessionUserSeq, actualUserSeq);

            return ResponseEntity.ok(updated);

        } catch (IllegalStateException e) {
            log.warn("[예약 수정 실패] {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            log.error("장례 예약 수정 중 오류", e);
            return ResponseEntity.internalServerError().body("예약 수정 실패");
        }
    }


    /* =========================================================
     *  [5] 장례 예약 삭제
     * ========================================================= */
    @DeleteMapping({"/funeral_reserve/{id}", "/admin/reserve/funeral_reserve/{id}"})
    @ResponseBody
    public ResponseEntity<?> deleteFuneralReserve(
            @PathVariable Long id,
            @RequestParam(required = false) String targetUserSeq,
            HttpServletRequest request,
            @SessionAttribute(value = "loginUser", required = false) Object loginUser) {

        try {
            if (loginUser == null)
                return ResponseEntity.status(401).body("로그인이 필요합니다.");

            String uri = request.getRequestURI();
            boolean isAdminRequest = uri.startsWith("/reserve/admin/");
            String sessionUserSeq = extractUserSeq(loginUser);

            String actualUserSeq = isAdminRequest
                    ? (targetUserSeq != null ? targetUserSeq : sessionUserSeq)
                    : sessionUserSeq;

            funeralReserveService.deleteForAdminOrUser(id, actualUserSeq, isAdminRequest);

            return ResponseEntity.ok("삭제 완료");

        } catch (Exception e) {
            log.error("예약 삭제 실패", e);
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    /* =========================================================
     * Ourpage (추모 공간) 예약
     * ========================================================= */
    @GetMapping("/ourpage/main")
    public String ourpageMain(Model model, HttpSession session) { // HttpSession 추가
        // 1. 목록 가져오기
        List<OurpageReserveDto> list = ourpageReserveService.getAllOurpages();
        model.addAttribute("ourpageList", list);

        // 2. 로그인 여부 확인해서 HTML로 보냄
        boolean isLoggedIn = session.getAttribute("userSeq") != null;
        model.addAttribute("isLoggedIn", isLoggedIn);

        return "ourpage/ourpage";
    }

    /* [1] Ourpage 예약 페이지 (신규/수정) */
    @GetMapping("/ourpage_reserve")
    public String ourpageReserveForm(@RequestParam(required = false) Long id,
                                     @RequestParam(required = false) Integer slotIndex, // [추가] 자리 번호 받기
                                     Model model,
                                     HttpSession session) {
        String userSeq = (String) session.getAttribute("userSeq");
        if (id != null) {
            // Service에 findById가 구현되어 있어야 합니다.
            OurpageReserveDto dto = ourpageReserveService.findById(id);
            if (dto == null) return "redirect:/ourpage/main";
            model.addAttribute("reserve", dto);
            model.addAttribute("mode", "edit");
        } else {
            model.addAttribute("reserve", null);
            model.addAttribute("mode", "create");
            model.addAttribute("slotIndex", slotIndex);
        }
        model.addAttribute("sessionUserSeq", userSeq);
        return "reserve/Ourpage_reserve";
    }

    /* [2] Ourpage 상세 조회 */
    @GetMapping("/api/ourpage_reserve/{id}")
    @ResponseBody
    public ResponseEntity<?> getOurpageReserve(@PathVariable Long id) {
        try {
            OurpageReserveDto dto = ourpageReserveService.findById(id);
            if (dto == null) return ResponseEntity.notFound().build();
            return ResponseEntity.ok(dto);
        } catch (Exception e) {
            log.error("[Ourpage 상세조회 실패]", e);
            return ResponseEntity.internalServerError().body("정보를 불러올 수 없습니다.");
        }
    }

    /* [3] Ourpage 저장 (파일 포함 -> @RequestParam 사용) */
    @PostMapping("/save4")
    @ResponseBody
    public ResponseEntity<?> saveOurpageReserve(
            @RequestParam("petName") String petName,
            @RequestParam("dateStart") String dateStart,
            @RequestParam("dateEnd") String dateEnd,
            @RequestParam("message") String message,
            @RequestParam("slotIndex") Integer slotIndex,
            @RequestParam(value = "petPhoto", required = false) MultipartFile petPhoto,
            @SessionAttribute(value = "loginUser", required = false) Object loginUser) {

        try {
            if (loginUser == null) return ResponseEntity.status(401).body("로그인이 필요합니다.");

            // UserSeq를 String으로 추출 (지난번 수정 사항 유지)
            String userSeq = extractUserSeq(loginUser);

            ourpageReserveService.save(petName, dateStart, dateEnd, message, petPhoto, userSeq, slotIndex);

            log.info("[Ourpage 예약 등록 완료] userSeq={}, slotIndex={}", userSeq, slotIndex);
            return ResponseEntity.ok("ok");
        } catch (Exception e) {
            log.error("Ourpage 저장 실패", e);
            return ResponseEntity.internalServerError().body("저장 실패");
        }
    }

    /* [4] Ourpage 수정 */

    @PutMapping("/ourpage_reserve/{id}")
    @ResponseBody
    public ResponseEntity<?> updateOurpageReserve(
            @PathVariable Long id,
            @RequestParam("petName") String petName,
            @RequestParam("dateStart") String dateStart,
            @RequestParam("dateEnd") String dateEnd,
            @RequestParam("message") String message,
            @RequestParam(value = "petPhoto", required = false) MultipartFile petPhoto,
            @SessionAttribute(value = "loginUser", required = false) Object loginUser) {

        try {
            if (loginUser == null) return ResponseEntity.status(401).body("로그인이 필요합니다.");
            String userSeq = extractUserSeq(loginUser);

            ourpageReserveService.updateReserve(id, petName, dateStart, dateEnd, message, petPhoto, userSeq);

            log.info("[Ourpage 수정 완료] ID={}, userSeq={}", id, userSeq);
            return ResponseEntity.ok("ok");
        } catch (Exception e) {
            log.error("Ourpage 수정 실패", e);
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /* [5] Ourpage 삭제 */
    @DeleteMapping("/ourpage_reserve/{id}")
    @ResponseBody
    public ResponseEntity<?> deleteOurpageReserve(
            @PathVariable Long id,
            @SessionAttribute(value = "loginUser", required = false) Object loginUser) {
        try {
            if (loginUser == null) return ResponseEntity.status(401).body("로그인이 필요합니다.");
            String userSeq = extractUserSeq(loginUser);

            // Service에 delete 구현 필요
            ourpageReserveService.deleteReserve(id, userSeq);

            log.info("[Ourpage 삭제 완료] ID={}, userSeq={}", id, userSeq);
            return ResponseEntity.ok("삭제 완료");
        } catch (Exception e) {
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
