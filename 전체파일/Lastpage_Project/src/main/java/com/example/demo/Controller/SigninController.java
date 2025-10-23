package com.example.demo.Controller;


import com.example.demo.Domain.Common.Dto.PsyReserveDto;
import com.example.demo.Domain.Common.Dto.SignupDto;
import com.example.demo.Domain.Common.Entity.PsyReserve;
import com.example.demo.Domain.Common.Entity.Signup;
import com.example.demo.Domain.Common.Service.FuneralReserveService;
import com.example.demo.Domain.Common.Service.SignupService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller

@Slf4j
@RequestMapping
@RequiredArgsConstructor
public class SigninController {

    private final SignupService signupService;

    @GetMapping("/signin")//http://localhost:8099/signin
    public String home(){
        System.out.println("GET /");
        log.info("GET /....");
        return "signin/Signin";
    }

    @PostMapping("/userinfoSave")
    @ResponseBody
    public ResponseEntity<?> saveUserInfo(@RequestBody SignupDto dto) {
        try {
            Signup saved = signupService.saveUserInfo(dto);
            log.info("예약 저장 완료!!: {}", saved.getId());

            // JS에서 redirect할 수 있도록 email을 응답에 담아줌
            return ResponseEntity.ok(saved.getId());
        } catch (Exception e) {
            log.error(" 예약 저장 실패!!", e);
            return ResponseEntity.internalServerError().body("예약 저장 실패!!");
        }
    }


}
