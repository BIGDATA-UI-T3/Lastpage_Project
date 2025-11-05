////package com.example.demo.Controller;
////
////import com.example.demo.Domain.Common.Dto.ReserveDto;
////import com.example.demo.Domain.Common.Entity.FuneralReserve;
////import com.example.demo.Domain.Common.Service.FuneralReserveService;
////import lombok.RequiredArgsConstructor;
////import lombok.extern.slf4j.Slf4j;
////import org.springframework.http.ResponseEntity;
////import org.springframework.stereotype.Controller;
////import org.springframework.web.bind.annotation.*;
////
////@Controller
////@Slf4j
////@RequestMapping("/reserve")
////@RequiredArgsConstructor
////public class ReserveController {
////
////    private final FuneralReserveService funeralReserveService;
////
////    // 페이지 이동 (GET 요청)
////    @GetMapping("/Funeral_reserve")
////    public String reservePage() {
////        return "reserve/Funeral_reserve";  // templates/reserve/psy_reserve.html
////    }
////
////    // 예약 저장 (AJAX POST)
////    @PostMapping("/save")
////    @ResponseBody
////    public ResponseEntity<?> saveReserve(@RequestBody ReserveDto dto) {
////        try {
////            FuneralReserve saved = funeralReserveService.saveReservation(dto);
////            log.info(" 예약 저장 완료: {}", saved.getOwnerName());
////            return ResponseEntity.ok(saved);
////        } catch (Exception e) {
////            log.error(" 예약 저장 실패", e);
////            return ResponseEntity.internalServerError().body("예약 저장 실패");
////        }
////    }
////}
//
//package com.example.demo.Controller;
//
//import com.example.demo.Domain.Common.Dto.ReserveDto;
//import com.example.demo.Domain.Common.Security.CustomUserPrincipal;
//import com.example.demo.Domain.Common.Service.FuneralReserveService;
//import com.example.demo.Domain.Common.Entity.FuneralReserve;
//import lombok.RequiredArgsConstructor;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.core.annotation.AuthenticationPrincipal;
//import org.springframework.stereotype.Controller;
//import org.springframework.ui.Model;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.List;
//
//@Controller
//@RequiredArgsConstructor
//public class ReserveController {
//
//    private final FuneralReserveService reserveService;
//
//    @GetMapping("/reserve")
//    public String showReserveForm(Model model) {
//        model.addAttribute("reserveDto", new ReserveDto()); // 폼 바인딩용
//        return "funeral_reserve"; // templates/funeral_reserve.html 렌더링
//    }
//
//    @PostMapping("/save")
//    public ResponseEntity<?> saveReserve(@RequestBody ReserveDto dto, Authentication auth) {
//        // 로그인 사용자 확인
//        System.out.println("예약 저장 시도: " + auth.getName());
//        // 저장 로직
//        return ResponseEntity.ok("예약 완료");
//    }
//
//    /** 예약 등록 */
//    @PostMapping("/reserve")
//    public String createReserve(@ModelAttribute ReserveDto dto,
//                                @AuthenticationPrincipal CustomUserPrincipal userPrincipal) {
//        String username = userPrincipal.getUsername();
//        reserveService.createReserve(dto, username);
//        return "redirect:/mypage";
//    }
//
////    /** 마이페이지 예약 목록 조회 */
////    @GetMapping("/mypage")
////    public String myPage(Model model,
////                         @AuthenticationPrincipal CustomUserPrincipal userPrincipal) {
////        String username = userPrincipal.getUsername();
////        List<FuneralReserve> reserves = reserveService.getReservesByUsername(username);
////        model.addAttribute("reserves", reserves);
////        return "mypage";
////    }
////
////    /** 예약 수정 */
////    @PostMapping("/reserve/{id}/edit")
////    public String updateReserve(@PathVariable Long id,
////                                @ModelAttribute ReserveDto dto,
////                                @AuthenticationPrincipal CustomUserPrincipal userPrincipal) {
////        reserveService.updateReserve(id, dto, userPrincipal.getUsername());
////        return "redirect:/mypage";
////    }
////
////    /** 예약 취소 */
////    @PostMapping("/reserve/{id}/cancel")
////    public String cancelReserve(@PathVariable Long id,
////                                @AuthenticationPrincipal CustomUserPrincipal userPrincipal) {
////        reserveService.cancelReserve(id, userPrincipal.getUsername());
////        return "redirect:/mypage";
////    }
//}
//
////@GetMapping("/reserve")
////public String showReserveForm(Model model) {
////    model.addAttribute("reserveDto", new ReserveDto());
////    return "funeral_reserve"; // 새 경로 반영
////}
//
//



package com.example.demo.Controller;

import com.example.demo.Domain.Common.Dto.ReserveDto;
import com.example.demo.Domain.Common.Security.CustomUserPrincipal;
import com.example.demo.Domain.Common.Service.FuneralReserveService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@RequestMapping("/reserve")
public class ReserveController {

    private final FuneralReserveService reserveService;

    @GetMapping("/")
    public String showNewReserveForm(Model model,
                                  @AuthenticationPrincipal CustomUserPrincipal userPrincipal) {
        if (userPrincipal == null) {
            return "redirect:/signin"; // 로그인 안 되어 있을 때 처리
        }
        model.addAttribute("reserveDto", new ReserveDto());
        model.addAttribute("loginUser", userPrincipal); // 로그인 상태 체크용
        return "funeral_reserve"; // templates/funeral_reserve.html
    }

    // JS에서 fetch(`/reserve/detail/${id}`) 호출 시 데이터 반환
    @GetMapping("/detail/{id}")
    @ResponseBody
    public ResponseEntity<?> getReserveDetail(@PathVariable Long id,
                                              @AuthenticationPrincipal CustomUserPrincipal userPrincipal) {
        if (userPrincipal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인 필요");
        }

        ReserveDto dto = reserveService.getReserveDtoByIdAndUsername(id, userPrincipal.getUsername());
        if (dto == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("예약 정보를 찾을 수 없습니다.");
        }

        return ResponseEntity.ok(dto); // JSON 형태로 ReserveDto 반환
    }

    // JS fetch용 POST API는 @ResponseBody 적용
    @PostMapping("/save")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> saveReserve(@RequestBody ReserveDto dto,
                                                           @AuthenticationPrincipal CustomUserPrincipal userPrincipal) {
        Map<String, Object> result = new HashMap<>();
        try {
            if (userPrincipal == null) {
                result.put("status", "fail");
                result.put("message", "로그인 필요");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(result);
            }

            String username = userPrincipal.getUsername();
            dto.setUsername(username);

            reserveService.createReserve(dto, username);

            result.put("status", "success");
            result.put("message", "예약 저장 완료");
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            e.printStackTrace();
            result.put("status", "fail");
            result.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
        }
    }

    // 기존 예약폼 불러오기 (수정용)
    @GetMapping({"/form","/form/{id}"})
    public String showEditReserveForm(@PathVariable(required = false) Long id,
                                      @AuthenticationPrincipal CustomUserPrincipal userPrincipal,
                                      Model model) {
        if (userPrincipal == null) {
            return "redirect:/signin";
        }

        ReserveDto dto;

        if (id != null) { // 수정용
            dto = reserveService.getReserveDtoByIdAndUsername(id, userPrincipal.getUsername());
            if (dto == null) {
                return "redirect:/mypage";
            }
        } else { // 신규
            dto = new ReserveDto();
        }

        model.addAttribute("reserveDto", dto);

        // JSON 형태로도 추가 — 여기 추가하는 게 핵심
        try {
            model.addAttribute("reserveDtoJson", new ObjectMapper().writeValueAsString(dto));
        } catch (Exception e) {
            model.addAttribute("reserveDtoJson", "{}"); // 파싱 실패 대비
        }

        return "funeral_reserve";
    }



    // 기존 예약폼 불러오기 (수정용)
//    @GetMapping("/edit/{id}")
//    @ResponseBody
//    public ReserveDto getReserveForEdit(@PathVariable Long id,
//                                        @AuthenticationPrincipal CustomUserPrincipal userPrincipal) {
//        String username = userPrincipal.getUsername();
//        return reserveService.getReserveDtoByIdAndUsername(id, username);
//    }


    // 수정 저장
    @PostMapping("/update/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateReserve(@PathVariable Long id,
                                                             @RequestBody ReserveDto dto,
                                                             @AuthenticationPrincipal CustomUserPrincipal userPrincipal) {
        Map<String, Object> result = new HashMap<>();
        try {
            if (userPrincipal == null) {
                result.put("status", "fail");
                result.put("message", "로그인 필요");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(result);
            }

            reserveService.updateReserve(id, dto, userPrincipal.getUsername());
            result.put("status", "success");
            result.put("message", "예약 수정 완료");
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            result.put("status", "fail");
            result.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
        }
    }

    // 삭제
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Map<String, Object>> deleteReserve(@PathVariable Long id,
                                                             @AuthenticationPrincipal CustomUserPrincipal userPrincipal) {
        Map<String, Object> result = new HashMap<>();
        try {
            if (userPrincipal == null) {
                result.put("status", "fail");
                result.put("message", "로그인 필요");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(result);
            }

            reserveService.deleteReserve(id, userPrincipal.getUsername());
            result.put("status", "success");
            result.put("message", "예약 삭제 완료");
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            result.put("status", "fail");
            result.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
        }
    }
}

